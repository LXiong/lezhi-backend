package com.buzzinate.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

/**
 * This class is used for DateTime manipulation
 * 
 * @author John Chen Sep 12, 2009 Copyright 2009 Buzzinate Co. Ltd.
 */
public final class DateTimeUtil {
	public static final long MILLISECONDS_MINUTE = 60000;
	public static final long MILLISECONDS_HOUR = 3600000;
	public static final long MILLISECONDS_DAY = 86400000;
	public static final long MILLISECONDS_WEEK = 604800000;

	/**
	 * FMT_DATE_YYYY_MM_DD 返回 2010-01-01 the following const is to define date
	 * format.
	 */
	public static final int FMT_DATE_YYYY_MM_DD = 1;

	/**
	 * 
	 * FMT_DATE_YYYYMMDD 返回 20100101
	 */
	public static final int FMT_DATE_YYYYMMDD = 2;

	public static final int FMT_DATE_YYMMDD = 3;

	public static final int FMT_DATE_YYYY = 4;

	public static final int FMT_DATE_YYMM = 5;

	/**
	 * yyyyMM 返回 201011
	 */
	public static final int FMT_DATE_YYYYMM = 6;

	/**
	 * yyyy-MM-dd HH:mm:ss 返回 2010-11-02 16:27:30
	 */
	public static final int FMT_DATE_YYYY_MM_DD_HH_MM_SS = 7;

	/**
	 * yyyyMMddHHmm 返回 201011021623
	 */
	public static final int FMT_DATE_YYYYMMDDHHMM = 8;

	/**
	 * yyyyMMddHHmmss 返回 20101205144039
	 */
	public static final int FMT_DATE_YYYYMMDDHHMMSS = 9;

	private DateTimeUtil() {
	}

	/**
	 * Convert "yyyy-MM-dd" formatted date into date object.
	 * 
	 * @param dateStr
	 * @return
	 * @throws ParseException
	 */
	public static Date convertDate(String dateStr) throws ParseException {
		try {
			if (dateStr == null || dateStr.isEmpty())
				return null;
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = (Date) formatter.parse(dateStr);
			return date;
		} catch (ParseException e) {
			throw new ParseException("error parsing date: " + dateStr, 0);
		}
	}

	/**
	 * 获取中文日期时间.如：2010年12月05日
	 * 
	 * @param date
	 * @return
	 */
	public static String getChineseDate(Date date) {
		return getChineseDate(formatDate(date, FMT_DATE_YYYY_MM_DD));
	}

	public static String getChineseDate(String strDate) {
		if (strDate == null || strDate.length() < 10)
			return "";
		return strDate.substring(0, 4) + "\u5E74" 
				+ strDate.substring(5, 7) + "\u6708"
				+ strDate.substring(8, 10) + "\u65E5";
	}

	
	/**
	 * this function is to format date into a string @ param date the date to be
	 * formatted.
	 * 
	 * @param nFmt
	 *            FMT_DATE_YYYYMMDD to format string like 'yyyy-mm-dd' or to
	 *            format string like 'yyyy-mm-dd hh:mm:ss'
	 * @return the format string date.
	 */
	public static String formatDate(Date date, int nFmt) {
		SimpleDateFormat fmtDate = null;
		switch (nFmt) {
		default:
		case FMT_DATE_YYYY_MM_DD:
			fmtDate = new SimpleDateFormat("yyyy-MM-dd");
			break;
		case FMT_DATE_YYYYMMDD:
			fmtDate = new SimpleDateFormat("yyyyMMdd");
			break;
		case FMT_DATE_YYMMDD:
			fmtDate = new SimpleDateFormat("yyMMdd");
			break;
		case FMT_DATE_YYYY:
			fmtDate = new SimpleDateFormat("yyyy");
			break;
		case FMT_DATE_YYMM:
			fmtDate = new SimpleDateFormat("yyMM");
			break;
		case FMT_DATE_YYYYMM:
			fmtDate = new SimpleDateFormat("yyyyMM");
			break;
		case FMT_DATE_YYYY_MM_DD_HH_MM_SS:
			fmtDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			break;
		case FMT_DATE_YYYYMMDDHHMM:
			fmtDate = new SimpleDateFormat("yyyyMMddHHmm");
			break;
		case FMT_DATE_YYYYMMDDHHMMSS:
			fmtDate = new SimpleDateFormat("yyyyMMddHHmmss");
			break;
		}

		return fmtDate.format(date);
	}

	/**
	 * 从字段串参数返回 java.util.Date 日期类型 Method getDateByString.
	 * 
	 * @param strDate
	 * @param nFmt
	 *            输入的参数样式 如:20060809,2006-09-20
	 * @return the date.
	 */
	public static Date getDateByString(String strDate, int nFmt) {
		SimpleDateFormat fmtDate = null;
		switch (nFmt) {
		default:
		case FMT_DATE_YYYY_MM_DD:
			fmtDate = new SimpleDateFormat("yyyy-MM-dd");
			break;
		case FMT_DATE_YYYYMMDD:
			fmtDate = new SimpleDateFormat("yyyyMMdd");
			break;
		case FMT_DATE_YYMMDD:
			fmtDate = new SimpleDateFormat("yyMMdd");
			break;
		case FMT_DATE_YYYY:
			fmtDate = new SimpleDateFormat("yyyy");
			break;
		case FMT_DATE_YYMM:
			fmtDate = new SimpleDateFormat("yyMM");
			break;
		case FMT_DATE_YYYYMM:
			fmtDate = new SimpleDateFormat("yyyyMM");
			break;
		case FMT_DATE_YYYY_MM_DD_HH_MM_SS:
			fmtDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			break;
		case FMT_DATE_YYYYMMDDHHMM:
			fmtDate = new SimpleDateFormat("yyyyMMddHHmm");
			break;
		case FMT_DATE_YYYYMMDDHHMMSS:
			fmtDate = new SimpleDateFormat("yyyyMMddHHmmss");
			break;
		}
		java.util.Date dDate = null;
		try {
			dDate = fmtDate.parse(strDate);
		} catch (Exception e) {
			dDate = null;
		}
		return dDate;

	}

	/**
	 * Format the date into "yyyy-MM-dd" format.
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		return formatDate(date, "yyyy-MM-dd");
	}

	public static String formatDate(Date date, String format) {
		if (date == null || format == null)
			return "";
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format((java.util.Date) date);
	}

	/**
	 * Format the time into "HH:mm" format. You can pass in a String in the form
	 * of: h:m:s HH:mm:ss or any combination. It will return 00:00 if the string
	 * given is invalid.
	 * 
	 * @param time
	 * @return
	 */
	public static String formatTime(String time) {
		if (time == null)
			return "00:00";
		String[] s = time.split(":");
		if (s.length < 2) {
			return "00:00";
		}
		if (s[0].isEmpty() || s[1].isEmpty()) {
			return "00:00";
		}

		try {
			Integer.parseInt(s[0]);
			Integer.parseInt(s[1]);
		} catch (NumberFormatException n) {
			return "00:00";
		}

		String hour = "";
		if (s[0].length() == 1) {
			hour = "0" + s[0];
		} else if (s[0].length() == 2) {
			hour = s[0];
		} else {
			hour = "00";
		}

		String min = "";
		if (s[1].length() == 1) {
			min = "0" + s[1];
		} else if (s[1].length() == 2) {
			min = s[1];
		} else {
			min = "00";
		}
		return hour + ":" + min;
	}

	/**
	 * Gets the current date and time in a Date object.
	 * 
	 * @return
	 */
	public static Date getCurrentDate() {
		return new java.util.Date();
	}

	/**
	 * Get the current date day, without time
	 * 
	 * @return
	 */
	public static Date getCurrentDateDay() {
		return getDateDay(new java.util.Date());
	}

	/**
	 * Get the day and remove time from the specific time
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDateDay(Date date) {
		return DateUtils.truncate(date, Calendar.DATE);
	}

	/**
	 * Subtracts weeks from the given Date
	 * 
	 * @param d
	 * @param weeks
	 * @return
	 */
	public static Date subtractWeeks(Date d, int weeks) {
		return DateUtils.addWeeks(d, -weeks);
	}

	/**
	 * Subtracts days from the given Date
	 * 
	 * @param d
	 * @param days
	 * @return
	 */
	public static Date subtractDays(Date d, int days) {
		return DateUtils.addDays(d, -days);
	}

	/**
	 * Plus days from the given Date
	 * 
	 * @param d
	 * @param days
	 * @return
	 */
	public static Date plusDays(Date d, int days) {
		return DateUtils.addDays(d, days);
	}

	/**
	 * Plus months from the given Date and month amount.
	 * 
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date plusMonths(Date date, int amount) {
		return DateUtils.addMonths(date, amount);
	}

	/**
	 * Gets the difference in days between the given dates.
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static int getDaysDiff(Date startDate, Date endDate) {
		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.setTime(startDate);
		fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
		fromCalendar.set(Calendar.MINUTE, 0);
		fromCalendar.set(Calendar.SECOND, 0);
		fromCalendar.set(Calendar.MILLISECOND, 0);

		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTime(endDate);
		toCalendar.set(Calendar.HOUR_OF_DAY, 0);
		toCalendar.set(Calendar.MINUTE, 0);
		toCalendar.set(Calendar.SECOND, 0);
		toCalendar.set(Calendar.MILLISECOND, 0);

		return (int) (toCalendar.getTime().getTime() - fromCalendar.getTime()
				.getTime())
				/ (1000 * 60 * 60 * 24);
	}
}
