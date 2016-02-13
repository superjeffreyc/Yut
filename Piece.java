public class Piece {

	Player player;
	int location = 0;

	Piece(Player player) {
		this.player = player;
	}

	public void getPlayer() {
		return this.player;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public int getLocation() {
		return this.location;
	}
}
