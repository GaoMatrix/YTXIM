/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.speedtong.example.common.utils;

import java.io.File;

import com.speedtong.example.ui.manager.CCPAppManager;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

/**
 * <p>Title: ProjectConstant.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: Beijing Speedtong Information Technology Co.,Ltd</p>
 * @author Jorstin Chan
 * @date 2015-1-21
 * @version 4.0
 */
public class ProjectConstant {

	public static final String[] YTX_EMPLEYEE = readEmplyee();
	
	public static String[] readEmplyee() {

		if(YTX_EMPLEYEE != null) {
			return YTX_EMPLEYEE;
		}
		
		try {

			Workbook book = Workbook.getWorkbook(CCPAppManager.getContext().getAssets().open("ytx_employee.xls"));
			Sheet sheet = book.getSheet(0);
			System.out.println(sheet.getRows());
			String[] apkCodeStrings = new String[sheet.getRows()];

			for (int i = 0; i < sheet.getRows(); i++) {
				Cell cell1 = sheet.getCell(0, i);
				String result = cell1.getContents();
				apkCodeStrings[i] = result.trim();
			}

			book.close();
			return apkCodeStrings;

		} catch (Exception e) {		

			e.printStackTrace();
			return null;
		}

	}
}
