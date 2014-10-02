package game.object.monster;

import game.framework.FileReader;
import game.framework.World;
import game.object.AggressiveMonster;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

public class Skeleton extends AggressiveMonster {
	/** Location of file description spawn positions */
	private static final String SPAWN_FILE = "assets/spawn/skeleton.txt";
	/** The location of the image */
	private static final String IMAGE_LOC = "assets/units/skeleton.png";
	
	/* Stats */
	private static final String NAME = "Skeleton";
	private static final int HEALTH = 100;
	private static final int DAMAGE = 16;
	private static final int COOLDOWN = 500;
	
	/** Image file imported one and then stored statically */
	private static Image image;
	
	/** Load spawn locations from a file and create as many skeletons as appropriate
	 * @param world The world in which the skeletons live
	 * @return An arraylist of the created skeletons
	 */
	public static ArrayList<Skeleton> spawnAll(World world) {
		List<String> spawnList = FileReader.readFile(SPAWN_FILE);
		ArrayList<Skeleton> result = new ArrayList<Skeleton>();
		for (String s:spawnList) {
			String[] coords = s.split(",");
			Vector2f pos = new Vector2f();
			try {
				pos.x = Integer.parseInt(coords[0]);
				pos.y = Integer.parseInt(coords[1]);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.print(e.getMessage());
				continue;
			} catch (NumberFormatException e) {
				System.out.print(e.getMessage());
				continue;			
			}
			result.add(new Skeleton(pos, world));
		}
		return result;
	}
	
	/** Fetches the pre-generated image for the class, or if it has not yet been generated, does so.
	 * @return Image for the skeleton's sprite
	 */
	private static Image getImage() {
		try {
			if (image == null) {
				image = new Image(IMAGE_LOC);
			}
			return image;
		} catch (SlickException e) {
			System.out.println(e.getMessage());
			System.exit(1);
			return null;
		}
	}
	
	/** Constructor creates an object of the class
	 * @param pos Position at which it is created
	 * @param world The world in which it lives
	 */
	public Skeleton(Vector2f pos, World world) {
		super(pos, getImage(), world, NAME, HEALTH, DAMAGE, COOLDOWN);
		// TODO Auto-generated constructor stub
	}
}