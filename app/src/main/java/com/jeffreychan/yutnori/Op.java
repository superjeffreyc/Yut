package com.jeffreychan.yutnori;

/*
 * Indicates the type of click being made. Used for multi-player.
 */
public class Op {
	public static byte CLICK_ROLL_BUTTON = 0;
	public static byte CLICK_FINISH = 1;
	public static byte CLICK_OFF_BOARD_PIECE = 2;
	public static byte CLICK_TILE = 3;
	public static byte CLICK_PLAYER = 4;
	public static byte CLICK_HIDE_TILES = 5;
	public static byte CLICK_EMPTY = 6;
	public static byte ACK = 7;

}
