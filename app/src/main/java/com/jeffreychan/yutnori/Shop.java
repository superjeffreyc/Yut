package com.jeffreychan.yutnori;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/*
 * This class controls Shop functions and serves as the interface to SharedPreferences, where avatar selections are stored.
 */
public class Shop {

	public static Shop Instance = new Shop();	// Only one instance can exist

	private LinkedHashMap<String, Integer> costs = new LinkedHashMap<>();		// Holds cost for each avatar
	private LinkedHashMap<String, Integer[]> drawables = new LinkedHashMap<>();	// Holds images for each avatar

	// Used to access stored information on device
	SharedPreferences prefs;
	Editor editor;

	String playerAnimals;		// Currently selected avatars
	String lastSavedAnimals;	// Avatars that were previously saved (not updated until the user presses save)

	/*
	 * This constructor lists all the possible avatars in the game.
	 *
	 * To add a new avatar, follow the following format and add it to the bottom of the constructor
	 *
	 *  addAvatar(NAME, COST, new Integer[]{
	 *      static image drawable
	 *      animation1
	 *      animation2
	 *      animation3
	 *      animation4
	 *      finished avatar
	 *      player icon
	 *  });
	 *
	 *  NOTE: NAME cannot have spaces in it
	 *
	 */
	private Shop(){

		addAvatar("Seal", 0, new Integer[]{
				R.drawable.seal1jump1,
				R.drawable.seal_animation1,
				R.drawable.seal_animation2,
				R.drawable.seal_animation3,
				R.drawable.seal_animation4,
				R.drawable.seal_goal,
				R.drawable.seal_icon
		});

		addAvatar("Penguin", 0, new Integer[]{
				R.drawable.penguin1jump1,
				R.drawable.penguin_animation1,
				R.drawable.penguin_animation2,
				R.drawable.penguin_animation3,
				R.drawable.penguin_animation4,
				R.drawable.penguin_goal,
				R.drawable.penguin_icon
		});

		addAvatar("Rabbit", 10, new Integer[]{
				R.drawable.rabbit1jump1,
				R.drawable.rabbit_animation1,
				R.drawable.rabbit_animation2,
				R.drawable.rabbit_animation3,
				R.drawable.rabbit_animation4,
				R.drawable.rabbit_goal,
				R.drawable.rabbit_icon
		});

		addAvatar("Bird", 10, new Integer[]{
				R.drawable.bird1jump1,
				R.drawable.bird_animation1,
				R.drawable.bird_animation2,
				R.drawable.bird_animation3,
				R.drawable.bird_animation4,
				R.drawable.bird_goal,
				R.drawable.bird_icon
		});

		addAvatar("Hedgehog", 10, new Integer[]{
				R.drawable.hedgehog1jump1,
				R.drawable.hedgehog_animation1,
				R.drawable.hedgehog_animation2,
				R.drawable.hedgehog_animation3,
				R.drawable.hedgehog_animation4,
				R.drawable.hedgehog_goal,
				R.drawable.hedgehog_icon
		});

		addAvatar("Fennec-Fox", 10, new Integer[]{
				R.drawable.fennecfox1jump1,
				R.drawable.fennecfox_animation1,
				R.drawable.fennecfox_animation2,
				R.drawable.fennecfox_animation3,
				R.drawable.fennecfox_animation4,
				R.drawable.fennecfox_goal,
				R.drawable.fennecfox_icon
		});

		addAvatar("Polar-Bear", 50, new Integer[]{
				R.drawable.polarbear1jump1,
				R.drawable.polarbear_animation1,
				R.drawable.polarbear_animation2,
				R.drawable.polarbear_animation3,
				R.drawable.polarbear_animation4,
				R.drawable.polarbear_goal,
				R.drawable.polarbear_icon
		});

		addAvatar("Leopard", 60, new Integer[]{
				R.drawable.leopard1jump1,
				R.drawable.leopard_animation1,
				R.drawable.leopard_animation2,
				R.drawable.leopard_animation3,
				R.drawable.leopard_animation4,
				R.drawable.leopard_goal,
				R.drawable.leopard_icon
		});

		addAvatar("Husky", 100, new Integer[]{
				R.drawable.husky1jump1,
				R.drawable.husky_animation1,
				R.drawable.husky_animation2,
				R.drawable.husky_animation3,
				R.drawable.husky_animation4,
				R.drawable.husky_goal,
				R.drawable.husky_icon
		});
	}

	/*
	 * Helper method for constructor
	 * Adds the avatar into the game
	 */
	private void addAvatar(String name, int cost, Integer[] images){
		costs.put(name, cost);
		drawables.put(name, images);
	}

	/*
	 * Called from the title screen and loads any stored avatar preferences
	 */
	protected void initializeShop(Context context){
		prefs = context.getSharedPreferences("avatar", Context.MODE_PRIVATE);
		editor = prefs.edit();

		playerAnimals = prefs.getString("animals", "Seal Penguin");
		lastSavedAnimals = playerAnimals;

		if (prefs.getInt("Seal", 0) == 0 || prefs.getInt("Penguin", 0) == 0){
			playerAnimals = "Seal Penguin";
			editor.putString("animals", playerAnimals);
			editor.putInt("Seal", 1);
			editor.putInt("Penguin", 1);
			editor.putInt("coins", 0);
			editor.commit();
		}

		// debug
		//editor.putInt("coins", 250);
		//editor.commit();

	}

	/**
	 * Gets the static image associated with an animal
	 * @param s The string name of the animal
	 * @return The static image drawable of the animal
	 */
	protected Integer getImage(String s){
		return drawables.get(s)[0];
	}

	/**
	 * Gets the index associated with an animal
	 * @param s The string name of the animal
	 * @return The index of the animal
	 */
	protected Integer getIndex(String s){
		ArrayList<String> list = getUnlockedAvatars();
		for (int i = 0; i < list.size(); i++){
			if (s.equalsIgnoreCase(list.get(i))) return i;
		}
		return -1;  // Error - cannot find animal
	}

	/**
	 * Gets the cost associated with an animal
	 * @param s The string name of the animal
	 * @return The cost of the animal
	 */
	protected Integer getCost(String s){
		return costs.get(s);
	}

	/**
	 * Gets the animation associated with an animal
	 * @param s The string name of the animal
	 * @return The animation drawable of the animal
	 */
	protected Integer getAnim(String s){
		return drawables.get(s)[1];
	}

	/**
	 * This method attempts to buy an animal in the avatar shop
	 *
	 * Checks to make sure the user has enough coins available to make the purchase
	 * If successful, the proper amount of coins will be deducted and saved in SharedPreferences and return true
	 * Otherwise, this method returns false
	 *
	 * @param animal The string name of the animal to purchase
	 * @return Boolean value indicating whether the purchase was successful
	 */
	protected boolean makePurchase(String animal){
		int cost = getCost(animal);
		int coins = prefs.getInt("coins", 0);

		if (coins >= cost) {
			editor.putInt(animal, 1);
			coins -= Shop.Instance.getCost(animal);
			editor.putInt("coins", coins);
			editor.commit();

			return true;
		}
		return false;
	}

	/**
	 * Adds coins to the user's account (after playing a game)
	 *
	 * @param amount The amount of coins to be added
	 */
	protected void addCoins(int amount){
		int coins = prefs.getInt("coins", 0);
		coins += amount;
		editor.putInt("coins", coins);
		editor.commit();
	}

	/**
	 * Gets the current coins the user has
	 *
	 * @return The number of coins the user has
	 */
	protected int getCoins(){
		return prefs.getInt("coins", 0);
	}

	/**
	 * Determines which avatars the user has not unlocked yet and returns that list
	 *
	 * @return An ArrayList of all the locked avatars
	 */
	protected ArrayList<String> getLockedAvatars(){
		ArrayList<String> list = new ArrayList<>();
		for (String str : drawables.keySet()){
			if (prefs.getInt(str, 0) == 0){
				list.add(str);
			}
		}
		return list;
	}

	/**
	 * Determines which avatars the user already unlocked and returns that list
	 * Seal and Penguin are already unlocked by default
	 *
	 * @return An ArrayList of all unlocked avatars
	 */
	protected ArrayList<String> getUnlockedAvatars(){
		ArrayList<String> list = new ArrayList<>();
		for (String str : drawables.keySet()){
			if (prefs.getInt(str, 0) != 0){
				list.add(str);
			}
		}
		return list;
	}

	/**
	 * Returns the set of images and animations associated with an animal
	 * @param s The string name of the animal
	 * @return An Integer array of drawables and animations
	 */
	protected Integer[] getImageArray(String s){ return drawables.get(s); }

	/**
	 * Gets the current animals for player 1 and player 2
	 * @return A String array (size 2) of the player animal names
	 */
	protected String[] getAnimals(Context context){

		// Make sure the shop is initialized
		if (playerAnimals == null) {
			Shop.Instance.initializeShop(context);
		}
		return playerAnimals.split("\\s+");
	}

	protected void switchAvatars(){
		String[] s = playerAnimals.split(" ");
		String p1 = s[0];
		String p2 = s[1];
		playerAnimals = p2 + " " + p1;
	}

	/**
	 * Resets the animal selections for each player back to the last previously saved avatar selection
	 */
	protected void reset(){
		playerAnimals = lastSavedAnimals;
	}

	/**
	 * Save the current selection of avatars into SharedPreferences
	 */
	protected void saveAvatars(Context context){
		editor.putString("animals", playerAnimals);
		editor.commit();

		lastSavedAnimals = playerAnimals;

		Toast savedToast = Toast.makeText(context, "Saved changes", Toast.LENGTH_SHORT);
		savedToast.show();
	}

	/**
	 * Temporarily change the avatar of a player
	 * Changes are not applied here
	 *
	 * @param player The player that will have its avatar changed
	 * @param item The avatar to change to
	 */
	protected void changeAvatar(int player, String item, Context context){
		String[] s = getAnimals(context);

		if (player == 1){
			playerAnimals = item + " " + s[1];
		} else {
			playerAnimals = s[0] + " " + item;
		}
	}
}
