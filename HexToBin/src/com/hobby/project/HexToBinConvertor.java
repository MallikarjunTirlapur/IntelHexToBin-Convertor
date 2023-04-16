/**
 * Copyright (c) 2016, Mallikarjun Tirlapur All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.hobby.project;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * Class reads the hex file from a given path, processes hex and writes
 * converted binary in to a file at a location provided by the user.
 *
 * @author Mallikarjun Tirlapur
 */
public class HexToBinConvertor {
	BufferedReader readFile;
	/*
	 * address/data linked hash table, table maintains the access order in which
	 * keys are inserted in to the map
	 */
	LinkedHashMap<Long, String> dataAddressHashMap;

	/**
	 * Hex file is read and inserted line by line into hash table and processed.
	 *
	 * @param inPath
	 *            local path to hex file
	 */
	public void readHex(String inPath) throws IOException {
		String line;
		/* instance of class LineParser */
		LineParser lnPrsr = new LineParser();
		LinkedHashMap<Integer, String> linTbl = new LinkedHashMap<Integer, String>();

		/* read hex file */
		readFile = new BufferedReader(new FileReader(inPath));
		for (int i = 0; (line = readFile.readLine()) != null; i++) {
			/* read hex file line by line and insert into the hash table */
			linTbl.put(i, line);
		}

		/* process all the lines to get bin data */
		dataAddressHashMap = lnPrsr.processRecord(linTbl);
	}

	/**
	 * If given the start and end address, the function writes only the data
	 * which lies within the given address. and loads the buffer with the
	 * decoded bin data from the hex file and write buffer into a bin file.
	 *
	 * @param outPath
	 *            local path to bin file
	 * @param strtAddrs
	 *            start address from which buffer is loaded
	 * @param endAdds
	 *            end address at which buffer load is stopped
	 */
	private void writeBin(String outPath, long strtAddrs, long endAdds) throws IOException {
		byte[] buffer;
		File fl = new File(outPath);
		
		/* create a new file if it does't exist already */
		fl.getParentFile().mkdirs();
		fl.createNewFile();

		/* write data into the file */
		DataOutputStream os = new DataOutputStream(new FileOutputStream(outPath));

		/* get all the keys(address) from the hash table */
		Set<Long> keys = dataAddressHashMap.keySet();

		/* create an array of keys */
		Object[] arr = keys.toArray();

		/* calculate the size of the buffer to load bin data */
		long buffSize = ((Long) arr[arr.length - 1] - (Long) arr[0])
				+ (dataAddressHashMap.get(arr[arr.length - 1]).length() / 2);

		for (long recAddrs : keys) {
			/* get the associated data for the key (address) */
			byte[] data = getBytes(dataAddressHashMap.get(recAddrs));

			/* craete a buffer */
			buffer = new byte[(int) data.length];

			/* fill the buffer with 0xff (NOPs) */
			Arrays.fill(buffer, (byte) 0xff);
			
			if (endAdds != 0) {
				/*
				 * skip the loading if address won't lie with in the start end
				 * address boundaries
				 */
				if ((recAddrs >= strtAddrs) && ((recAddrs + data.length) <= endAdds)) {
					/*
					 * load buffer with the data when given end address from the
					 * user is not 0
					 */
					System.arraycopy(data, 0, buffer, 0, data.length);
				}
				else
				{
					break;
				}
			} else {
				/*
				 * load buffer with the data when given end address from the
				 * user is 0
				 */
				System.arraycopy(data, 0, buffer, 0, data.length);
			}
			os.write(buffer);
		}

		os.close();

		
		/* print start address, end address and total number of bytes */
		if (endAdds != 0) {
			System.out.print("Start Address: " + strtAddrs);
			System.out.print("\nEnd Address: " + endAdds);
		} else {
			System.out.print("\nStart Address: " + arr[0]);
			System.out.print("\nEnd Address: " + arr[arr.length - 1]);
		}
		System.out.print("\nTotal Number Of Bytes: " + buffSize);
	}

	/**
	 * converts string into a byte array
	 *
	 * @param data
	 *            String data
	 * @return bytes byte array
	 */
	private byte[] getBytes(String data) {
		byte[] bytes = new byte[data.length() / 2];
		for (int i = 0; i < data.length(); i += 2) {
			bytes[i / 2] = (byte) (Integer.parseInt(data.substring(i, i + 2), 16) & 0xff);
		}
		return bytes;
	}

	/**
	 * print the usage doc for no argument or for argument with -h option.
	 */
	private static void usageDoc() {
		StringBuilder strbldr = new StringBuilder();
		strbldr.append("\n");
		strbldr.append("The command line tool efficiently converts Intel format .hex file to .bin file \n");
		strbldr.append(
				"with the right command line arguments as shown below. For more info please visit my github page at ...link \n);"
						+ "Moreover, the source code is made public and accessible at \n");
		strbldr.append("....................link............................................................ \n");
		strbldr.append("Usage: \n");
		strbldr.append("IntelHexToBin <option> .hex <option> .bin <option> 0xYYYY <option> 0xZZZZ \n");
		strbldr.append("options: \n");
		strbldr.append("-h --help \n");
		strbldr.append("   IntelHexToBin -h \n");
		strbldr.append("-i --input path .hex file \n");
		strbldr.append("-o --output path .bin file \n");
		strbldr.append("   IntelHexToBin -i xx/yy/zz.hex -o xx/yy/zz.bin \n");
		strbldr.append("-s --start address \n");
		strbldr.append("-e --end address \n");
		strbldr.append("   IntelHexToBin -i xx/yy/zz.hex -o xx/yy/zz.bin -s 0xYYYY -e 0xZZZZ \n");
		System.out.print(strbldr.toString());
	}

	/**
	 * The application starting point.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		int arg;
		long strtAddrs = 0;
		long endAddrs = 0;
		String hexPath = "";
		String binPath = "";
		String stOt = "";
		HexToBinConvertor hTb = new HexToBinConvertor();

		/* the vector of list of valid long options */
		LongOpt[] lgOt = new LongOpt[5];
		lgOt[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		lgOt[1] = new LongOpt("hexfile", LongOpt.REQUIRED_ARGUMENT, null, 'i');
		lgOt[2] = new LongOpt("binfile", LongOpt.REQUIRED_ARGUMENT, null, 'o');
		lgOt[3] = new LongOpt("start address", LongOpt.REQUIRED_ARGUMENT, null, 's');
		lgOt[4] = new LongOpt("end address", LongOpt.REQUIRED_ARGUMENT, null, 'e');

		for (LongOpt lg : lgOt) {
			stOt += (char) lg.getVal();
			if (lg.getHasArg() == LongOpt.REQUIRED_ARGUMENT) {
				stOt += ":";
			}
		}
		
		Getopt gt = new Getopt("IntelHexToBin", args, stOt, new LongOpt[lgOt.length]);
		/* print usage doc for no arguments from the user */
		if (args.length < 1) {
			usageDoc();
			System.exit(0);
		}

		while ((arg = gt.getopt()) != -1) {
			switch (arg) {
			case 'h':
				/* print usage doc for -h option from the user */
				usageDoc();
				System.exit(0);

			case 'i':
				/* read input hex file path */
				hexPath = gt.getOptarg();
				break;

			case 'o':
				/* read output bin file path */
				binPath = gt.getOptarg();
				break;

			case 's': {
				/* get the start address if provided */
				String agmt = gt.getOptarg();
				if (agmt.matches("^0[xX][0-9a-fA-F]+$")) {
					strtAddrs = Long.parseLong(agmt.replaceFirst("^0[xX]", ""), 16);
				} else {
					System.out.print("Invalid Start Address \n");
				}
			}
				break;

			case 'e': {
				/* get the end address if provided */
				String agmt = gt.getOptarg();
				if (agmt.matches("^0[xX][0-9a-fA-F]+$")) {
					endAddrs = Long.parseLong(agmt.replaceFirst("^0[xX]", ""), 16);
				} else {
					System.out.print("Invalid End Address \n");
				}
				break;
			}
			}
		}
		try {
			hTb.readHex(hexPath);
			hTb.writeBin(binPath, strtAddrs, endAddrs);
		} catch (Exception e) {
			System.out.print("File access failed!!!!!provide a vaild input/output path");
		}
	}

}
