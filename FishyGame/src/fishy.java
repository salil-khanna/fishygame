import java.awt.Color;
import java.util.Random;
import javalib.funworld.*;
import javalib.worldimages.*;
import tester.Tester;

// a class named Aquarium, extending the world in order to makeScenes, onTick, etc.
class Aquarium extends World {
  int width = 500;
  int height = 500;
  
  Fish fish;
  ILoFish backgroundFish; 
  int currentTick;
  Random rand;
  
  // constructor
  public Aquarium(Fish fish, ILoFish backgroundFish, int currentTick, Random rand) {
    super();
    this.fish = fish;
    this.backgroundFish = backgroundFish;
    this.currentTick = currentTick;
    this.rand = rand;
  }

  // a method returning a worldScene, making a Scene based on the player and background fish
  public WorldScene makeScene() {
    WorldImage backgroundImage = new RectangleImage(width, height, OutlineMode.SOLID, Color.CYAN);
    WorldScene backgroundScene = this.getEmptyScene()
                                     .placeImageXY(backgroundImage, width / 2, height / 2);
    return this.fish.drawPlayer(this.backgroundFish.drawBFish(backgroundScene));
  }
  
  // a method returning a new World based on which key was pressed
  public World onKeyEvent(String ke) {
    return new Aquarium(this.fish.moveFish(ke), this.backgroundFish, this.currentTick, this.rand);
  }
  
  // a method returning a new World based on what is occurring to each aspect of the current world
  public World onTick() {
    return new Aquarium(this.fish.newSize(this.backgroundFish), 
        this.backgroundFish.newILoFish(this.fish)
                           .moveAllFish()
                           .addBackFish(this.currentTick, this.rand), 
        this.currentTick + 1, this.rand);
  }
  
  // a method returning a new WorldEnd based on if a player has lost, won, or neither
  public WorldEnd worldEnds() {
    if (this.backgroundFish.eatPlayer(this.fish)) {
      return new WorldEnd(true, this.lastScene("You were eaten!"));
    }
    if (this.fish.isLargest()) {
      return new WorldEnd(true, this.lastScene("You win!"));
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  } 
  
  // returns a WorldScene for the lastScene to be displayed to the user of the program
  public WorldScene lastScene(String s) {
    return this.makeScene().placeImageXY(new TextImage(s, Color.red), width / 2, height / 2);
  }
}


// an interface made to hold fish and background fish
interface IFish {
  int WIDTH = 500;
  int HEIGHT = 500;
  
  // method to determine if the a fish can eat another fish
  public boolean eatsFish(AFish other);
  
  // determines if two fish are on top of each other/eating radius
  public boolean isOverlapping(AFish other);
}

// an abstract class to hold common fields and methods of fish and background fish
// implements the interface IFish
abstract class AFish implements IFish {
  Posn loc;
  int size;
  Color color;
  boolean dir;
  
  // constructor
  AFish(Posn loc, int size, Color color, boolean dir) {
    this.loc = loc;
    this.size = size;
    this.color = color;
    this.dir = dir;
  }
  
  // returns a WorldImage, containing a drawing of the fish
  public WorldImage displayFish() {
    WorldImage fishyBody = new CircleImage(10, OutlineMode.SOLID, this.color);
    WorldImage fishyFinsLeft = new TriangleImage(new Posn(0, 0), new Posn(15, 10), 
        new Posn(15, -10), OutlineMode.SOLID, this.color);
    WorldImage fishyFinsRight = new TriangleImage(new Posn(0, 0), new Posn(-15, 10), 
        new Posn(-15, -10), OutlineMode.SOLID, this.color);
    WorldImage fishyBodyFinsLeft = new OverlayImage(fishyBody, fishyFinsLeft);
    WorldImage fishyBodyFinsRight = new OverlayImage(fishyBody, fishyFinsRight);
    WorldImage fishyTailLeft = new TriangleImage(new Posn(0, 0), new Posn(8, 10), 
        new Posn(8, -10), OutlineMode.SOLID, this.color);
    WorldImage fishyTailRight = new TriangleImage(new Posn(0, 0), new Posn(-8, 10), 
        new Posn(-8, -10), OutlineMode.SOLID, this.color);
    WorldImage fishyMouth = new OverlayOffsetImage(new ScaleImage(fishyBody, 0.10), 0, 2, 
        new ScaleImage(fishyBody, 0.10));
    
    if (this.dir) {
      WorldImage fishyBodyFinsMouth = new OverlayOffsetImage(fishyBodyFinsRight, 10, 0, fishyMouth);
      WorldImage fishyWhole = new OverlayOffsetImage(fishyTailRight, 12, 0, fishyBodyFinsMouth);
      return new ScaleImage(fishyWhole.movePinhole(2, 0), this.size * 0.5);
    }
    else {
      WorldImage fishyBodyFinsMouth = new OverlayOffsetImage(fishyBodyFinsLeft, -10, 0, fishyMouth);
      WorldImage fishyWhole = new OverlayOffsetImage(fishyBodyFinsMouth, 12, 0, fishyTailLeft);
      return new ScaleImage(fishyWhole.movePinhole(-2, 0), this.size * 0.5);
    }
  }
  
  // method to determine if the a fish can eat another fish
  public boolean eatsFish(AFish another) {
    return this.size > another.size && this.isOverlapping(another);
  }
  
  // determines if two fish are on top of each other/eating radius
  public boolean isOverlapping(AFish other) {
    WorldImage fishyBodyThis = new ScaleImage(new CircleImage(10, 
        OutlineMode.SOLID, this.color), this.size * 0.5);
    WorldImage fishyBodyOther = new ScaleImage(new CircleImage(10, 
        OutlineMode.SOLID, this.color), other.size * 0.5);
    return this.lengthApart(other) <= fishyBodyThis.getWidth() * 0.5 
        + fishyBodyOther.getWidth() * 0.5;
  }
  
  
  // method to determine how far apart two fish are
  public int lengthApart(AFish other) {
    int lengthApartX = Math.abs(this.loc.x - other.loc.x);
    int lengthApartY = Math.abs(this.loc.y - other.loc.y);

    return (int) Math.sqrt(Math.pow(lengthApartX, 2) + Math.pow(lengthApartY, 2));
  }

  // an abstract method moving the background fish
  public abstract AFish moveBackgroundFish();
  
  // an abstract method generating a new Fish to be spawned in
  public AFish generateNewFish(Random rand) {
    
    boolean randomDir = rand.nextInt(10) % 2 == 0;
      
    int randomSize = rand.nextInt(100);
      
    if (randomSize > 0 && randomSize <= 30) {
      return new BackgroundFish(this.randomLoc(randomDir, rand), 1, Color.white, randomDir);
    }
    else if (randomSize > 30 && randomSize <= 40) {
      return new BackgroundFish(this.randomLoc(randomDir, rand), 2, Color.magenta, randomDir);
    }
    else if (randomSize > 40 && randomSize <= 60) {
      return new BackgroundFish(this.randomLoc(randomDir, rand), 3, Color.blue, randomDir);
    }
    else if (randomSize > 60 && randomSize <= 70) {
      return new BackgroundFish(this.randomLoc(randomDir, rand), 4, Color.red, randomDir);
    }
    else if (randomSize > 70 && randomSize <= 78) {
      return new BackgroundFish(this.randomLoc(randomDir, rand), 5, Color.green, randomDir);
    }
    else if (randomSize > 78 && randomSize <= 85) {
      return new BackgroundFish(this.randomLoc(randomDir, rand), 6, Color.yellow, randomDir);
    }
    else if (randomSize > 85 && randomSize <= 91) {
      return new BackgroundFish(this.randomLoc(randomDir, rand), 7, Color.pink, randomDir);
    }
    else if (randomSize > 91 && randomSize <= 96) {
      return new BackgroundFish(this.randomLoc(randomDir, rand), 8, Color.gray, randomDir);
    }
    else {
      return new BackgroundFish(this.randomLoc(randomDir, rand),  9, Color.black, randomDir);
    }
  }
  
  // (HELPER) method to generate a random position according to a provided direction
  Posn randomLoc(boolean dirRight, Random rand) {
    if (dirRight) {
      return new Posn(-50, rand.nextInt(HEIGHT));
    }
    else {
      return new Posn(WIDTH + 50, rand.nextInt(HEIGHT));
    }    
  }
}

// a class representing the player fish, extending the AFish abstract class
class Fish extends AFish {

  Fish(Posn loc, int size, Color color, boolean dir) {
    super(loc, size, color, dir);
  }

  // returns a new Fish based on what key is pressed
  Fish moveFish(String ke) {
    int adjustedSpeed = (12 / size) + 3;
    
    if (ke.equals("right")) {
      return new Fish(new Posn((this.loc.x + adjustedSpeed) % WIDTH, 
          this.loc.y), this.size, this.color, true);
    } else if (ke.equals("left")) {
      return new Fish(new Posn(((this.loc.x - adjustedSpeed + WIDTH) 
          % WIDTH), this.loc.y), this.size, this.color, false);
    } else if (ke.equals("up")) {
      return new Fish(new Posn(this.loc.x, ((this.loc.y - adjustedSpeed 
          + HEIGHT) % HEIGHT)), this.size, this.color, this.dir);
    } else if (ke.equals("down")) {
      return new Fish(new Posn(this.loc.x, (this.loc.y + adjustedSpeed) 
          % HEIGHT), this.size, this.color, this.dir);
    } else {
      return this;
    }
    
  }    

  // increases the size of the fish when it eats another fish
  public Fish newSize(ILoFish other) {
    return new Fish(this.loc, this.size + other.sizeEaten(this), this.color, this.dir);
  }

  // an abstract method moving the background fish
  public AFish moveBackgroundFish() {
    return null;
  }
  
  // returning a WorldScene, drawing the player fish over the current scene
  public WorldScene drawPlayer(WorldScene accImage) {
    return accImage.placeImageXY(this.displayFish(), this.loc.x, this.loc.y);
  }
  
  // determines if the player fish is a certain size to declare as the finner
  public boolean isLargest() {
    return this.size >= 10;
  }
  
  // an method generating an AFish to be spawned in
  public AFish generateNewFish(Random rand) {
    return null;
  }
}

// a class representing the Background fish, extending the AFish abstract class
class BackgroundFish extends AFish {
  
  //constructor
  BackgroundFish(Posn loc, int size, Color color, boolean dir) {
    super(loc, size, color, dir);
  }
  
  // an abstract method moving the background fish
  public AFish moveBackgroundFish() {  
    int adjustedSpeed = 10 / size;
    
    if (this.dir) {
      return new BackgroundFish(new Posn(this.loc.x + adjustedSpeed, this.loc.y),
          this.size, this.color, true);
    }
    else {
      return new BackgroundFish(new Posn(this.loc.x - adjustedSpeed, this.loc.y),
          this.size, this.color, false);
    } 
  }
 
}


// represents all of the BackgroundFish
interface ILoFish {
  int WIDTH = 500;
  int HEIGHT = 500;

  // moves all the background fish
  ILoFish moveAllFish();
  
  // determines if the player is within eating size of a fish
  boolean eatFish(AFish player);
  
  // determines if a fish is within eating size of a player
  boolean eatPlayer(AFish player);
  
  // determines the size of the fish eaten
  int sizeEaten(AFish player);
  
  // to update this ILoFish when the an OtherFish is eaten
  ILoFish newILoFish(AFish player);
  
  // a method that returns a WorldScene, drawing all the fish on the current scene
  WorldScene drawBFish(WorldScene accImage);
  
  // a method that adds new fish to an existing ILoFish
  ILoFish addBackFish(int nowTick, Random rand);
}


// a class representing an empty list of fish, implementing the ILoFish interface
class MtLoFish implements ILoFish {

  // moves all the background fish
  public ILoFish moveAllFish() {
    return this;
  }
  
  // determines if the player is within eating size of a fish
  public boolean eatFish(AFish player) {
    return false;
  }
  
  // determines if a fish is within eating size of a player
  public boolean eatPlayer(AFish player) {
    return false;
  }
  
  // determines the size of the fish eaten
  public int sizeEaten(AFish player) {
    return 0;
  } 
  
  // a method that returns a WorldScene, drawing all the fish on the current scene
  public WorldScene drawBFish(WorldScene accImage) {
    return accImage;
  }
  
  // to update this ILoFish when the an OtherFish is eaten
  public ILoFish newILoFish(AFish player) {
    return this;
  }
  
  // a method that adds new fish to an existing ILoFish
  public ILoFish addBackFish(int nowTick, Random rand) {
    AFish dummyFish = new BackgroundFish(new Posn(100, 100), 1, Color.white, true);
    return new ConsLoFish(dummyFish.generateNewFish(rand), this);
  }
}


// a class representing an cons-ed list of fish, implementing the ILoFish interface
class ConsLoFish implements ILoFish {
  AFish first;
  ILoFish rest;

  // constructor
  ConsLoFish(AFish first, ILoFish rest) {
    this.first = first;
    this.rest = rest;
  }
  
  // moves all the background fish
  public ILoFish moveAllFish() {
    return new ConsLoFish(this.first.moveBackgroundFish(), this.rest.moveAllFish());
  }
  
  // determines if the player is within eating size of a fish
  public boolean eatFish(AFish player) {
    return player.eatsFish(this.first) || this.rest.eatFish(player);
  }
  
  // determines if a fish is within eating size of a player
  public boolean eatPlayer(AFish player) {
    return this.first.eatsFish(player) || this.rest.eatPlayer(player);
  }
  
  // determines the size of the fish eaten
  public int sizeEaten(AFish player) {
    if (player.eatsFish(this.first)) {
      return this.first.size;
    }
    else {
      return this.rest.sizeEaten(player);
    }
  }
  
  // a method that returns a WorldScene, drawing all the fish on the current scene
  public WorldScene drawBFish(WorldScene accImage) {
    return this.rest.drawBFish(accImage.placeImageXY(this.first.displayFish(), 
        this.first.loc.x, this.first.loc.y));
  }

  // to update this ILoFish when a BackgroundFish is eaten
  public ILoFish newILoFish(AFish player) {
    if (player.eatsFish(this.first)) {
      return this.rest.newILoFish(player);
    }
    else {
      return new ConsLoFish(this.first, this.rest.newILoFish(player));
    }
  }
  
  // a method that adds new fish to an existing ILoFish
  public ILoFish addBackFish(int nowTick, Random rand) {
    if (nowTick % 15 == 0) {
      return new ConsLoFish(this.first.generateNewFish(rand), this);
    }
    else {
      return this;
    }
  }
}

/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////

// a class containing examples for all the fish
class FishExamples {
  int width = 500;
  int height = 500;
  
  Posn mapCenter = new Posn(this.width / 2, this.height / 2);
  Posn mapCenter2 = new Posn(this.width / 2 - 2, this.height / 2 - 2);
  Fish player1 = new Fish(this.mapCenter, 2, Color.ORANGE , true);
  Fish player2 = new Fish(this.mapCenter, 2, Color.red , false);
  Fish player3 = new Fish(this.mapCenter, 5, Color.red , false);
  Fish player4 = new Fish(this.mapCenter, 11, Color.ORANGE , true);
 
  
  WorldImage fishyBody1 = new CircleImage(10, OutlineMode.SOLID, Color.ORANGE);
  WorldImage fishyFinsRight1 = new TriangleImage(new Posn(0, 0), new Posn(-15, 10), 
      new Posn(-15, -10), OutlineMode.SOLID, Color.ORANGE);
  WorldImage fishyBodyFinsRight1 = new OverlayImage(fishyBody1, fishyFinsRight1);
  WorldImage fishyTailRight1 = new TriangleImage(new Posn(0, 0), new Posn(-8, 10), 
      new Posn(-8, -10), OutlineMode.SOLID, Color.ORANGE);
  WorldImage fishyMouth1 = new OverlayOffsetImage(new ScaleImage(fishyBody1, 0.10), 0, 2, 
      new ScaleImage(fishyBody1, 0.10));
  WorldImage fishyBodyFinsMouth1 = new OverlayOffsetImage(fishyBodyFinsRight1, 10, 0, fishyMouth1);
  WorldImage fishyWhole1 = new OverlayOffsetImage(fishyTailRight1, 12, 0, fishyBodyFinsMouth1);  
  WorldImage drawPlayer1 = new ScaleImage(fishyWhole1.movePinhole(2, 0), 2 * 0.5);
  
  WorldImage fishyBody2 = new CircleImage(10, OutlineMode.SOLID, Color.red);
  WorldImage fishyFinsLeft2 = new TriangleImage(new Posn(0, 0), new Posn(15, 10), 
      new Posn(15, -10), OutlineMode.SOLID, Color.red);
  WorldImage fishyBodyFinsLeft2 = new OverlayImage(fishyBody2, fishyFinsLeft2);
  WorldImage fishyTailLeft2 = new TriangleImage(new Posn(0, 0), new Posn(8, 10), 
      new Posn(8, -10), OutlineMode.SOLID, Color.red);
  WorldImage fishyMouth2 = new OverlayOffsetImage(new ScaleImage(fishyBody2, 0.10), 0, 2, 
      new ScaleImage(fishyBody2, 0.10));
  WorldImage fishyBodyFinsMouth2 = new OverlayOffsetImage(fishyBodyFinsLeft2, -10, 0, fishyMouth2);
  WorldImage fishyWhole2 = new OverlayOffsetImage(fishyBodyFinsMouth2, 12, 0, fishyTailLeft2);
  WorldImage drawPlayer2 = new ScaleImage(fishyWhole2.movePinhole(-2, 0), 2 * 0.5);
  
  Posn loc1 = new Posn(this.width / 10, this.height / 10);
  Posn loc2 = new Posn(this.width, this.height / 2 - 25);
  Posn loc3 = new Posn(this.width / 2, this.height - 10);
  Posn loc4 = new Posn(this.width / 3, this.height / 4);
  Posn loc5 = new Posn(this.width / 6, this.height / 8);
  Posn loc6 = new Posn(this.width / 5, this.height / 2);
 
  AFish backgroundFish1 = new BackgroundFish(this.loc1, 1, Color.red, true);
  AFish backgroundFish2 = new BackgroundFish(this.loc2, 1, Color.orange, true);
  AFish backgroundFish3 = new BackgroundFish(this.loc3, 4, Color.yellow, true);
  AFish backgroundFish4 = new BackgroundFish(this.loc4, 3, Color.green, false);
  AFish backgroundFish5 = new BackgroundFish(this.loc5, 2, Color.blue, true);
  AFish backgroundFish6 = new BackgroundFish(this.loc6, 1, Color.BLACK, false);
  AFish backgroundFish7 = new BackgroundFish(this.mapCenter, 1, Color.BLACK, false);
  AFish backgroundFish8 = new BackgroundFish(this.mapCenter2, 10, Color.BLACK, false);
  
  WorldImage fishyBody3 = new CircleImage(10, OutlineMode.SOLID, Color.green);
  WorldImage fishyFinsLeft3 = new TriangleImage(new Posn(0, 0), new Posn(15, 10), 
      new Posn(15, -10), OutlineMode.SOLID, Color.green);
  WorldImage fishyBodyFinsLeft3 = new OverlayImage(fishyBody3, fishyFinsLeft3);
  WorldImage fishyTailLeft3 = new TriangleImage(new Posn(0, 0), new Posn(8, 10), 
      new Posn(8, -10), OutlineMode.SOLID, Color.green);
  WorldImage fishyMouth3 = new OverlayOffsetImage(new ScaleImage(fishyBody3, 0.10), 0, 2, 
      new ScaleImage(fishyBody3, 0.10));
  WorldImage fishyBodyFinsMouth3 = new OverlayOffsetImage(fishyBodyFinsLeft3, -10, 0, fishyMouth3);
  WorldImage fishyWhole3 = new OverlayOffsetImage(fishyBodyFinsMouth3, 12, 0, fishyTailLeft3);
  WorldImage drawBackgroundFish4 = new ScaleImage(fishyWhole3.movePinhole(-2, 0), 3 * 0.5);
  
  WorldImage fishyBody4 = new CircleImage(10, OutlineMode.SOLID, Color.blue);
  WorldImage fishyFinsRight4 = new TriangleImage(new Posn(0, 0), new Posn(-15, 10), 
      new Posn(-15, -10), OutlineMode.SOLID, Color.blue);
  WorldImage fishyBodyFinsRight4 = new OverlayImage(fishyBody4, fishyFinsRight4);
  WorldImage fishyTailRight4 = new TriangleImage(new Posn(0, 0), new Posn(-8, 10), 
      new Posn(-8, -10), OutlineMode.SOLID, Color.blue);
  WorldImage fishyMouth4 = new OverlayOffsetImage(new ScaleImage(fishyBody4, 0.10), 0, 2, 
      new ScaleImage(fishyBody4, 0.10));
  WorldImage fishyBodyFinsMouth4 = new OverlayOffsetImage(fishyBodyFinsRight4, 10, 0, fishyMouth4);
  WorldImage fishyWhole4 = new OverlayOffsetImage(fishyTailRight4, 12, 0, fishyBodyFinsMouth4);
  WorldImage drawBackgroundFish5 = new ScaleImage(fishyWhole4.movePinhole(2, 0), 2 * 0.5);
  
  ILoFish mt = new MtLoFish();
  ILoFish aquarium1 = new ConsLoFish(backgroundFish1, mt);
  ILoFish aquarium2 = new ConsLoFish(backgroundFish2, aquarium1);
  ILoFish aquarium3 = new ConsLoFish(backgroundFish3, aquarium2);
  ILoFish aquarium4 = new ConsLoFish(backgroundFish4, aquarium3);
  ILoFish aquarium5 = new ConsLoFish(backgroundFish5, aquarium4);
  ILoFish aquarium6 = new ConsLoFish(backgroundFish6, aquarium5);
  ILoFish aquarium7 = new ConsLoFish(backgroundFish7, aquarium1);
  ILoFish aquarium8 = new ConsLoFish(backgroundFish8, aquarium1);
  ILoFish aquarium9 = new ConsLoFish(backgroundFish4, mt);
  ILoFish aquarium10 = new ConsLoFish(backgroundFish5, mt);
  
  Aquarium scene1 = new Aquarium(this.player1, this.aquarium6, 10, new Random());
  Aquarium scene2 = new Aquarium(this.player4, this.aquarium1, 5, new Random());
  Aquarium scene3 = new Aquarium(this.player1, this.aquarium8, 2, new Random(3));
  
  WorldImage backgroundImage1 = new RectangleImage(width, height, OutlineMode.SOLID, Color.CYAN);
  WorldScene backgroundScene1 = scene1.getEmptyScene().placeImageXY(
      backgroundImage1, width / 2, height / 2);
  
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  //TESTS FOR THE AFish ABSTRACT CLASS/////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  
  // tests for the displayFish method of AFish
  boolean testDisplayFish(Tester t) {
    return
        t.checkExpect(this.player1.displayFish(), this.drawPlayer1)
        && t.checkExpect(this.player2.displayFish(), this.drawPlayer2)
        && t.checkExpect(this.backgroundFish4.displayFish(), this.drawBackgroundFish4)
        && t.checkExpect(this.backgroundFish5.displayFish(), this.drawBackgroundFish5);   
    
  }
  
  // tests for the eatsFish method of AFish
  boolean testEatsFish(Tester t) {
    return
        t.checkExpect(this.player1.eatsFish(this.backgroundFish1), false)
        && t.checkExpect(this.player1.eatsFish(this.backgroundFish2), false)
        && t.checkExpect(this.player1.eatsFish(this.backgroundFish3), false) 
        && t.checkExpect(this.player1.eatsFish(this.backgroundFish7), true)
        && t.checkExpect(this.backgroundFish7.eatsFish(this.player1), false)
        && t.checkExpect(this.backgroundFish8.eatsFish(this.player1), true);
  }
  
  // tests for the isOverlapping method of AFish
  boolean testIsOverlapping(Tester t) {
    return
        t.checkExpect(this.player1.isOverlapping(this.backgroundFish1), false)
        && t.checkExpect(this.player1.isOverlapping(this.backgroundFish2), false)
        && t.checkExpect(this.player1.isOverlapping(this.backgroundFish3), false) 
        && t.checkExpect(this.player1.isOverlapping(this.backgroundFish4), false)
        && t.checkExpect(this.backgroundFish5.isOverlapping(this.player1), false)
        && t.checkExpect(this.player1.isOverlapping(this.backgroundFish6), false)
        && t.checkExpect(this.backgroundFish7.isOverlapping(this.player1), true)
        && t.checkExpect(this.player1.isOverlapping(this.backgroundFish8), true); 
  }
  
  // tests for the lengthApart method of AFish
  boolean testLengthApart(Tester t) {
    return
        t.checkExpect(this.player1.lengthApart(this.backgroundFish1), 282)
        && t.checkExpect(this.backgroundFish2.lengthApart(this.player1), 251)
        && t.checkExpect(this.player1.lengthApart(this.backgroundFish7), 0) 
        && t.checkExpect(this.player2.lengthApart(this.backgroundFish4), 150)
        && t.checkExpect(this.backgroundFish5.lengthApart(this.player2), 251)
        && t.checkExpect(this.player2.lengthApart(this.backgroundFish7), 0)
        && t.checkExpect(this.player3.lengthApart(this.backgroundFish6), 150)
        && t.checkExpect(this.backgroundFish7.lengthApart(this.player3), 0)
        && t.checkExpect(this.player3.lengthApart(this.backgroundFish8), 2);
  }
  
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  //TESTS FOR THE Fish CLASS///////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  
  // tests for the moveFish method of Fish
  boolean testMoveFish(Tester t) {
    return
        t.checkExpect(this.player1.moveFish("right"), 
            new Fish(new Posn(259, 250), 2, Color.ORANGE, true))
        && t.checkExpect(this.player1.moveFish("left"), 
            new Fish(new Posn(241, 250), 2, Color.ORANGE, false))
        && t.checkExpect(this.player1.moveFish("up"),
            new Fish(new Posn(250, 241), 2, Color.ORANGE, true))
        && t.checkExpect(this.player2.moveFish("down"), 
            new Fish(new Posn(250, 259), 2, Color.red, false))
        && t.checkExpect(this.player2.moveFish(""), this.player2);
  }
  
  // tests for the newSize method of Fish
  boolean testNewSize(Tester t) {
    return
        t.checkExpect(this.player1.newSize(this.mt), this.player1)
        && t.checkExpect(this.player1.newSize(this.aquarium1), this.player1)
        && t.checkExpect(this.player2.newSize(this.aquarium2), this.player2)
        && t.checkExpect(this.player3.newSize(this.aquarium3), this.player3)
        && t.checkExpect(this.player1.newSize(this.aquarium7), 
            new Fish(this.mapCenter, 3, Color.orange, true))
        && t.checkExpect(this.player3.newSize(this.aquarium7), 
            new Fish(this.mapCenter, 6, Color.red, false))
        && t.checkExpect(this.player3.newSize(this.aquarium6), 
            new Fish(this.mapCenter, 5, Color.red, false));     
  }
  
  // tests for the drawPlayer method of Fish
  boolean testDrawPlayer(Tester t) {
    return
        t.checkExpect(this.player1.drawPlayer(scene1.getEmptyScene()), 
            scene1.getEmptyScene().placeImageXY(drawPlayer1, 250, 250))
        && t.checkExpect(this.player2.drawPlayer(scene1.getEmptyScene()), 
            scene1.getEmptyScene().placeImageXY(drawPlayer2, 250, 250));
  }
 
  
  // tests for the isLargest method of Fish
  boolean testIsLargest(Tester t) {
    return
        t.checkExpect(this.player1.isLargest(), false) 
        && t.checkExpect(this.player2.isLargest(), false)
        && t.checkExpect(this.player4.isLargest(), true);
  }
  
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  //TESTS FOR THE BackgroundFish CLASS/////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////

  
  // tests for the moveBackgounrdFish method of BackgroundFish
  boolean testMoveBackgroundFish(Tester t) {
    return
        t.checkExpect(this.backgroundFish1.moveBackgroundFish(), 
            new BackgroundFish(new Posn(60, 50), 1, Color.red, true)) 
        && t.checkExpect(this.backgroundFish2.moveBackgroundFish(), 
            new BackgroundFish(new Posn(510, 225), 1, Color.orange, true)) 
        && t.checkExpect(this.backgroundFish3.moveBackgroundFish(), 
            new BackgroundFish(new Posn(252, 490), 4, Color.yellow, true))
        && t.checkExpect(this.backgroundFish5.moveBackgroundFish(),
            new BackgroundFish(new Posn(88, 62), 2, Color.blue, true));
  }
  
  // tests for the generateNewFish method of BackgroundFish
  boolean testGenerateNewFish(Tester t) {
    return t.checkExpect(this.backgroundFish1.generateNewFish(new Random(3)),
          new BackgroundFish(new Posn(-50, 210), 3, Color.blue, true))
        && t.checkExpect(this.backgroundFish2.generateNewFish(new Random(5)),
          new BackgroundFish(new Posn(550, 474), 8, Color.gray, false))
        && t.checkExpect(this.backgroundFish3.generateNewFish(new Random(5)),
            new BackgroundFish(new Posn(550, 474), 8, Color.gray, false))
          && t.checkExpect(this.backgroundFish4.generateNewFish(new Random(3)),
            new BackgroundFish(new Posn(-50, 210), 3, Color.blue, true));
  }
  
  // tests for the randomLoc helper method of BackgroundFish
  boolean testRandomLoc(Tester t) {
    return t.checkExpect(this.backgroundFish1.randomLoc(true, new Random(3)), new Posn(-50, 234))
        && t.checkExpect(this.backgroundFish2.randomLoc(true, new Random(5)), new Posn(-50, 487))
        && t.checkExpect(this.backgroundFish3.randomLoc(false, new Random(3)), new Posn(550, 234))
        && t.checkExpect(this.backgroundFish4.randomLoc(false, new Random(5)), new Posn(550, 487));
  }
  
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  //TESTS FOR THE ILoFish INTERFACE////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  
  // tests for the moveAllFish method of ILoFish
  boolean testMoveAllFish(Tester t) {
    return
        t.checkExpect(this.mt.moveAllFish(), this.mt)
        && t.checkExpect(this.aquarium1.moveAllFish(), 
            new ConsLoFish(new BackgroundFish(new Posn(60, 50), 1, Color.red, true),
                this.mt))
        && t.checkExpect(this.aquarium2.moveAllFish(), 
            new ConsLoFish(new BackgroundFish(new Posn(510, 225), 1, Color.orange, true),
            new ConsLoFish(new BackgroundFish(new Posn(60, 50), 1, Color.red, true),
                this.mt)))
        && t.checkExpect(this.aquarium7.moveAllFish(), 
            new ConsLoFish(new BackgroundFish(new Posn(240, 250), 1, Color.black, false),
            new ConsLoFish(new BackgroundFish(new Posn(60, 50), 1, Color.red, true),
                this.mt)));
  }
  
  // tests for the eatFish method of ILoFish
  boolean testEatFish(Tester t) {
    return
        t.checkExpect(this.mt.eatFish(this.player1), false)
        && t.checkExpect(this.aquarium1.eatFish(this.player1), false)
        && t.checkExpect(this.aquarium7.eatFish(this.player2), true)
        && t.checkExpect(this.aquarium7.eatFish(this.player1), true)
        && t.checkExpect(this.aquarium2.eatFish(this.player4), false);
  }
  
  // tests for the eatPlayer method of ILoFish
  boolean testEatPlayer(Tester t) {
    return
        t.checkExpect(this.mt.eatPlayer(this.player1), false)
        && t.checkExpect(this.aquarium1.eatPlayer(this.player1), false)
        && t.checkExpect(this.aquarium7.eatPlayer(this.player2), false)
        && t.checkExpect(this.aquarium7.eatPlayer(this.player4), false)
        && t.checkExpect(this.aquarium8.eatPlayer(this.player1), true)
        && t.checkExpect(this.aquarium8.eatPlayer(this.player2), true);
  }
  
  // tests for the sizeEaten method of ILoFish
  boolean testSizeEaten(Tester t) {
    return
        t.checkExpect(this.mt.sizeEaten(this.player1), 0)
        && t.checkExpect(this.aquarium1.sizeEaten(this.player1), 0)
        && t.checkExpect(this.aquarium7.sizeEaten(this.player2), 1)
        && t.checkExpect(this.aquarium7.sizeEaten(this.player4), 1)
        && t.checkExpect(this.aquarium8.sizeEaten(this.player1), 0)
        && t.checkExpect(this.aquarium8.sizeEaten(this.player2), 0);
  }
  
  // tests for the drawBFish method of ILoFish
  boolean testDrawBFish(Tester t) {
    return
        t.checkExpect(this.mt.drawBFish(scene1.getEmptyScene()), 
            scene1.getEmptyScene())
        && t.checkExpect(this.aquarium9.drawBFish(scene1.getEmptyScene()), 
            scene1.getEmptyScene().placeImageXY(drawBackgroundFish4, 167, 125))
        && t.checkExpect(this.aquarium10.drawBFish(scene1.getEmptyScene()), 
            scene1.getEmptyScene().placeImageXY(drawBackgroundFish5, 83, 63));
  }
  
  // tests for the newILoFish method of ILoFish
  boolean testNewILoFish(Tester t) {
    return
        t.checkExpect(this.aquarium1.newILoFish(this.player1), aquarium1)
        && t.checkExpect(this.mt.newILoFish(this.player1), mt)
        && t.checkExpect(this.aquarium2.newILoFish(this.player1), aquarium2)
        && t.checkExpect(this.aquarium3.newILoFish(this.player1), aquarium3)
        && t.checkExpect(this.aquarium7.newILoFish(this.player1), aquarium1);
  }
  
  // tests for the addBackFishMethod of ILoFish
  boolean testAddBackFish(Tester t) {
    return
        t.checkExpect(this.mt.addBackFish(30, new Random(3)), 
            new ConsLoFish(new BackgroundFish(new Posn(100, 100), 1, Color.white, true)
                                             .generateNewFish(new Random(3)), this.mt))
        && t.checkExpect(this.aquarium3.addBackFish(30, new Random(3)),
            new ConsLoFish(this.backgroundFish3.generateNewFish(new Random(3)), this.aquarium3))
        && t.checkExpect(this.aquarium3.addBackFish(31,  new Random(3)), this.aquarium3);
    
  }
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  //TESTS FOR THE Aquarium Class///////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  
  // tests for the onKeyEvent method of Aquarium
  boolean testOnKeyEvent(Tester t) {
    return 
        t.checkExpect(scene1.onKeyEvent("right"), 
            new Aquarium(new Fish(new Posn(259, 250), 2, Color.ORANGE, true),
            aquarium6, 10, new Random()))
        && t.checkExpect(scene1.onKeyEvent("left"),
            new Aquarium(new Fish(new Posn(241, 250), 2, Color.ORANGE, false),
                aquarium6, 10, new Random()))
        && t.checkExpect(scene1.onKeyEvent("up"),
            new Aquarium(new Fish(new Posn(250, 241), 2, Color.ORANGE, true),
                aquarium6, 10, new Random()))
        && t.checkExpect(scene1.onKeyEvent("down"), 
            new Aquarium(new Fish(new Posn(250, 259), 2, Color.ORANGE, true),
                aquarium6, 10, new Random()))
        && t.checkExpect(scene1.onKeyEvent(""),
            new Aquarium(player1, aquarium6, 10, new Random()));
  }
  
  // tests for the worldEnds method of Aquarium
  boolean testWorldEnds(Tester t) {
    return 
        t.checkExpect(scene1.worldEnds(), new WorldEnd(false, scene1.makeScene()))
        && t.checkExpect(scene2.worldEnds(), new WorldEnd(true, 
            scene1.lastScene("You win!")))
        && t.checkExpect(scene3.worldEnds(), new WorldEnd(true, 
            scene1.lastScene("You were eaten!")));
       
  }
  
  // tests for the lastScene method of Aquarium
  boolean testLastScene(Tester t) {
    return 
        t.checkExpect(scene1.lastScene("Hello World"), 
            scene1.makeScene().placeImageXY(new TextImage("Hello World", Color.red), 
                250, 250))
        && t.checkExpect(scene2.lastScene("You win!"),  
            scene2.makeScene().placeImageXY(new TextImage("You win!", Color.red), 
                250, 250))
        && t.checkExpect(scene3.lastScene("You were eaten!"),
            scene3.makeScene().placeImageXY(new TextImage("You were eaten!", Color.red), 
                250, 250));
       
  }
  
  // tests for the onTick method of Aquarium
  boolean testOnTick(Tester t) {
    return 
        t.checkExpect(scene1.onTick(), new Aquarium(player1.newSize(aquarium6), 
            aquarium6.newILoFish(player1).moveAllFish().addBackFish(scene1.currentTick, 
                scene1.rand), scene1.currentTick + 1, scene1.rand))
        && t.checkExpect(scene2.onTick(), new Aquarium(player4.newSize(aquarium1), 
            aquarium1.newILoFish(player4).moveAllFish().addBackFish(scene2.currentTick, 
                scene2.rand), scene2.currentTick + 1, scene2.rand));
        
  }
  
  //tests for the makeScene method of Aquarium
  boolean testMakeScene(Tester t) {
    return 
        t.checkExpect(scene1.makeScene(), scene1.fish.drawPlayer(
            scene1.backgroundFish.drawBFish(backgroundScene1)))
        && t.checkExpect(scene2.makeScene(), scene2.fish.drawPlayer(
            scene2.backgroundFish.drawBFish(backgroundScene1)));
        
  }

  
  // the testAquarium method below allows for one to visualize the game using bigBang
  // run and see everything play out!
  boolean testAquarium(Tester t) {
    Aquarium w = new Aquarium(this.player1, this.mt, 0, new Random());
    return w.bigBang(500, 500, 0.1);
  }
}