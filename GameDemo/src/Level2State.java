import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Level2State extends GameState {

	boolean active;
	boolean showLines;
	boolean caught = false;
	int lives;
	float playerX, playerY;
	float deltaTimeAverage;
	BSPNode BSPTreeRoot;
	String message;
	String command;
	ArrayList<Guard> guardList;
	public static int playerSize = 7, startX = 75, startY = 500;
	public static float upperX = 32, lowerX = 943, upperY = 80, lowerY = 525;
	public final static int policeX = 315, policeY = 220;

	public Level2State(int lives) {
		this.lives = lives;
		this.caught = false;
		this.showLines = false;
		this.command = "Playing";
		playerX = startX;
		playerY = startY;
		BSPTreeRoot = LevelsBuilder.buildBSPLevel2();
		startX = 216;
		startY = 40;
		upperX = 150;
		lowerX = 1008;
		upperY = 8;
		lowerY = 636;

	}

	public void enter(Object memento) {
		active = true;
		deltaTimeAverage = 0;
		this.lives = 3;
		this.caught = false;
		this.showLines = false;
		this.command = "Playing";
		playerX = startX;
		playerY = startY;
		this.guardList = new ArrayList<>();
		CreateGuardList();
	}

	private void CreateGuardList() {

		guardList.add(new Guard(554, 270, 554, 436));
		guardList.add(new Guard(686, 436, 686, 270));
		guardList.add(new Guard(810, 600, 364, 600));
		guardList.add(new Guard(550, 50, 960, 50));

	}

	public void processKeyReleased(int aKeyCode) {
		if (aKeyCode == KeyEvent.VK_Q) {
			this.command = "Reset";
			active = false;
		}
		if (aKeyCode == KeyEvent.VK_R) {
			this.showLines = !showLines;
		}

		if (aKeyCode == KeyEvent.VK_ESCAPE)
			System.exit(0);
	}

	public void processKeyPressed(int aKeyCode) {
		MoveCharacter(aKeyCode);
	}

	public void MoveCharacter(int aKeyCode) {
		switch (aKeyCode) {
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			checkLegalMove(playerX, (float) (playerY - (0.5 * deltaTimeAverage)));
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			checkLegalMove(playerX, (float) (playerY + (0.5 * deltaTimeAverage)));
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			checkLegalMove((float) (playerX - (0.5 * deltaTimeAverage)), playerY);
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			checkLegalMove((float) (playerX + (0.5 * deltaTimeAverage)), playerY);
			break;
		default:
			return;
		}
	}
	
	private boolean PlayerWin() {
		return LevelsBuilder.Level2Finish.getArea().contains(new Point2D.Float(playerX, playerY));
	}

	private void checkLegalMove(float x, float y) {
		if (x < upperX) {
			this.playerX = upperX;
			return;
		}
		if (x > lowerX) {
			this.playerX = lowerX;
			return;
		}
		if (y < upperY) {
			this.playerY = upperY;
			return;
		}
		if (y > lowerY) {
			this.playerY = lowerY;
			return;
		}
		if (BSPTree.ThereIsLineOfSight(new Line(new Point2D.Float(x, y), new Point2D.Float(this.playerX, this.playerY)),
				this.BSPTreeRoot)) {
			this.playerX = (float) x;
			this.playerY = (float) y;
		}

	}

	public void update(long deltaTime) {
		deltaTimeAverage = deltaTimeAverage * 0.4f + 0.5f * (float) deltaTime;
	}

	public boolean isActive() {
		return active;
	}

	public String next() {
		if (command.equals("Reset"))
			return "Welcome";
		if (command.equals("GameLost"))
			return "GameLost";
		if(command.equals("Level2Win"))
			return "GameWon";
		return "error state";
	}

	public void render(GameFrameBuffer aGameFrameBuffer) {
		if (this.caught) {
			this.caught = false;
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			lives--;
			if (lives == 0) {
				command = "GameLost";
				this.active = false;
			} else {
				playerX = 15;
				playerY = 15;
			}
		}

		Graphics g = aGameFrameBuffer.graphics();

		if (PlayerWin()) {
			this.command = "Level2Win";
			this.active=false;
		}
		drawMap(g, this.BSPTreeRoot);
		drawHeadline(g);
		drawLineOfSight(g);
		drawPolice(g);
		drawPlayer(g);

	}

	public void drawPlayer(Graphics g) {
		g.setColor(Color.red);
		g.fillOval((int) playerX, (int) playerY, playerSize, playerSize);
	}

	public void drawPolice(Graphics g) {
		g.setColor(Color.blue);
		for (Guard currentGuard : guardList) {
			currentGuard.UpdatePosition(deltaTimeAverage);
			g.fillOval((int) currentGuard.currentX, (int) currentGuard.currentY, playerSize, playerSize);
		}

	}

	public void drawLineOfSight(Graphics g) {
		for (Guard currentGuard : guardList) {
			if (BSPTree.ThereIsLineOfSight(new Line(new Point2D.Float((int) this.playerX, (int) this.playerY),
					new Point2D.Float(currentGuard.currentX, currentGuard.currentY)), this.BSPTreeRoot)) {
				
				if(!currentGuard.hasShot)
					currentGuard.FireBullet(this.playerX,this.playerY);
				
				
				
				g.setColor(Color.red);
				g.drawLine((int) playerX + (playerSize / 2), (int) playerY + (playerSize / 2),
						(int) currentGuard.currentX + (playerSize / 2), (int) currentGuard.currentY + (playerSize / 2));
				// ENABLE THESE LINES TO ALLOW GAME OVER BY LOSING
				//
				// this.caught = true; return;
				//
			} else if (this.showLines) {
				g.setColor(Color.cyan);
				g.drawLine((int) playerX + (playerSize / 2), (int) playerY + (playerSize / 2),
						(int) currentGuard.currentX + (playerSize / 2), (int) currentGuard.currentY + (playerSize / 2));
			}
		}
	}

	public void drawHeadline(Graphics g) {
		g.setColor(Color.WHITE);
		String sight = "on";
		if (this.showLines == false)
			sight = "off";
		message = "Lives: " + lives + ", Q = Menu , R = Show sight (" + sight + "),  current coordinates: " + playerX
				+ "," + playerY + "\n";
		message += " y2=" + guardList.get(1).currentY;
		g.drawString(message, 10, 10);
	}

	public void drawMap(Graphics g, BSPNode current) {
		if (current.isArea()) {
			g.setColor(current.isIn() ? Color.white : Color.gray);
			if (current.isFinishArea())
				g.setColor(Color.orange);
			g.fillPolygon(current.getArea());
		} else {
			drawMap(g, current.getLeftChild());
			drawMap(g, current.getRightChild());
		}
	}

}