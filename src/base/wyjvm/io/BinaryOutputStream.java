// Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
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

package wyjvm.io;

import java.io.*;

public class BinaryOutputStream extends OutputStream {
	protected OutputStream output;
	protected int value;
	protected int count;
	
	public BinaryOutputStream(OutputStream output) {
		this.output = output;
	}
			
	public void write(int i) throws IOException {
		if(count == 0) {
			output.write(i);
		} else {
			write_bits(i,8);
		}
	}		
	
	public void write(byte[] bytes) throws IOException {
		for(byte b : bytes) {
			write(b);
		}
	}
	
	public void write_u1(int w) throws IOException {
		if(count == 0) {
			output.write(w & 0xFF);
		} else {
			write_bits(w & 0xFF,8);
		}		
	}

	public void write_u2(int w) throws IOException {
		write_u1((w >> 8) & 0xFF);
		write_u1(w & 0xFF);
	}

	public void write_u4(int w) throws IOException {
		write_u1((w >> 24) & 0xFF);
		write_u1((w >> 16) & 0xFF);
		write_u1((w >> 8) & 0xFF);
		write_u1(w & 0xFF);
	}	
	
	public void write_bits(int bits, int n) throws IOException {
		for(int i=0;i<n;++i) {
			boolean bit = (bits & 1) == 1;
			writeBit(bit);
			bits = bits >> 1;
		}
	}	
	
	public void writeBit(boolean bit) throws IOException {
		value = value << 1;
		if(bit) {
			value |= 1;
		}
		count = count + 1;
		if(count == 8) {
			count = 0;
			output.write(value);
			value = 0;
		}
	}
	
	public void close() throws IOException {
		if(count != 0) {
			output.write(value);
		}
		output.close();
	}
}
