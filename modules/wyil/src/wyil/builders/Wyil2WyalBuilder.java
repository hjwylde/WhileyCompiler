// Copyright (c) 2012, David J. Pearce (djp@ecs.vuw.ac.nz)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright
//      notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright
//      notice, this list of conditions and the following disclaimer in the
//      documentation and/or other materials provided with the distribution.
//    * Neither the name of the <organization> nor the
//      names of its contributors may be used to endorse or promote products
//      derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL DAVID J. PEARCE BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package wyil.builders;

import java.io.IOException;
import java.util.*;

import wybs.lang.Build;
import wybs.lang.Builder;
import wyfs.lang.Path;
import wyil.builders.VcBranch.AssertOrAssumeScope;
import wyil.lang.*;
import wyil.transforms.RuntimeAssertions;
import wycc.util.Logger;
import wycc.util.Pair;
import wycs.syntax.Expr;
import wycs.syntax.WyalFile;

/**
 * Responsible for converting a Wyil file into a Wycs file which can then be
 * passed into the Whiley Constraint Solver (Wycs).  
 * 
 * @author David J. Pearce
 * 
 */
public class Wyil2WyalBuilder implements Builder {

	/**
	 * The master namespace for identifying all resources available to the
	 * builder. This includes all modules declared in the project being verified
	 * and/or defined in external resources (e.g. jar files).
	 */
	protected final Build.Project project;

	/**
	 * For logging information.
	 */
	protected Logger logger = Logger.NULL;

	private String filename;
	
	public Wyil2WyalBuilder(Build.Project project) {
		this.project = project;
	}
	
	public Build.Project project() {
		return project;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public Set<Path.Entry<?>> build(
			Collection<Pair<Path.Entry<?>, Path.Root>> delta)
			throws IOException {
		Runtime runtime = Runtime.getRuntime();
		long start = System.currentTimeMillis();
		long memory = runtime.freeMemory();
			
		// ========================================================================
		// Translate files
		// ========================================================================
		HashSet<Path.Entry<?>> generatedFiles = new HashSet<Path.Entry<?>>();
		for(Pair<Path.Entry<?>,Path.Root> p : delta) {
			Path.Entry<WyilFile> sf = (Path.Entry<WyilFile>) p.first();
			Path.Root dst = p.second();
			Path.Entry<WyalFile> df = (Path.Entry<WyalFile>) dst.create(sf.id(), WyalFile.ContentType);
			generatedFiles.add(df);
			WyalFile contents = build(sf.read());
			// Write the file into its destination
			df.write(contents);
			// Then, flush contents to disk in case we generate an assertion
			// error later. In principle, this should be unnecessary when
			// syntax errors are no longer implemented as exceptions.
			df.flush();
		}
		
		// ========================================================================
		// Done
		// ========================================================================

		long endTime = System.currentTimeMillis();
		logger.logTimedMessage("Wyil => Wyal: compiled " + delta.size()
				+ " file(s)", endTime - start, memory - runtime.freeMemory());
		
		return generatedFiles;
	}
		
	protected WyalFile build(WyilFile wyilFile) {
		this.filename = wyilFile.filename();

		// TODO: definitely need a better module ID here.
		final WyalFile wyalFile = new WyalFile(wyilFile.id(), filename);

		for (WyilFile.TypeDeclaration type : wyilFile.types()) {
			transform(type);
		}
		for (WyilFile.FunctionOrMethodDeclaration method : wyilFile.functionOrMethods()) {
			transform(method, wyilFile, wyalFile);
		}

		return wyalFile;
	}

	protected void transform(WyilFile.TypeDeclaration def) {

	}

	protected void transform(WyilFile.FunctionOrMethodDeclaration method,
			WyilFile wyilFile, WyalFile wycsFile) {
		for (WyilFile.Case c : method.cases()) {
			transform(c, method, wyilFile, wycsFile);
		}
	}

	protected void transform(WyilFile.Case methodCase,
			WyilFile.FunctionOrMethodDeclaration method, WyilFile wyilFile,
			WyalFile wycsFile) {

		if (!RuntimeAssertions.getEnable()) {
			// inline constraints if they have not already been done.
			RuntimeAssertions rac = new RuntimeAssertions(this, filename);
			methodCase = rac.transform(methodCase, method);
		}

		Type.FunctionOrMethod fmm = method.type();
		int paramStart = 0;

		Code.Block body = methodCase.body();

		VcBranch master = new VcBranch(method, body);

		for (int i = paramStart; i != fmm.params().size(); ++i) {
			Type paramType = fmm.params().get(i);
			master.write(i, new Expr.Variable("r" + Integer.toString(i)), paramType);
		}

		List<Code.Block> requires = methodCase.precondition();

		if (requires.size() > 0) {
			Code.Block block = new Code.Block(fmm.params().size());
			for(Code.Block precondition : requires) {
				block.addAll(precondition);
			}
			VcBranch precond = new VcBranch(method, block);

			AssertOrAssumeScope scope = new AssertOrAssumeScope(false, block.size(), Collections.EMPTY_LIST); 
			precond.scopes.add(scope);
			
			// FIXME: following seems like a hack --- there must be a more
			// elegant way of doing this?
			for (int i = paramStart; i != fmm.params().size(); ++i) {
				precond.write(i, master.read(i), master.typeOf(i));
			}

			Expr constraint = precond.transform(new VcTransformer(this,
					wycsFile, filename, true));

			precond.scopes.remove(precond.scopes.size()-1);
			master.add(constraint);
		}

		master.transform(new VcTransformer(this, wycsFile, filename, false));
	}		
}
