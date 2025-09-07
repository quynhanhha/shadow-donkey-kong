package game.core;

import bagel.Image;
import bagel.Input;
import bagel.util.Point;
import java.util.List;
import game.entities.Platform;

/**
 * Abstract base class for all game objects.
 */
public abstract class GameObject {
    protected Point position;
    protected Image image;

    public GameObject(String imagePath, double x, double y) {
        this.image = new Image(imagePath);
        this.position = new Point(x, y);
    }

    public void render() {
        image.draw(position.x, position.y);
    }

    public void update(Input input) {
        // Optional override in subclasses
    }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public Point getPosition() {
        return position;
    }

    public Image getImage() {
        return image;
    }
    
    /**
     * Check if this object overlaps with another object horizontally
     */
    protected boolean isHorizontallyOverlapping(GameObject other) {
        double[] thisBounds = getBoundsArray();
        double[] otherBounds = other.getBoundsArray();
        
        double thisLeft = thisBounds[0];
        double thisRight = thisBounds[2];
        double otherLeft = otherBounds[0];
        double otherRight = otherBounds[2];
        
        return thisRight > otherLeft && thisLeft < otherRight;
    }
    
    /**
     * Check if this object is close to standing on top of another object
     */
    protected boolean isCloseToTopOf(GameObject other, double tolerance) {
        double thisBottom = position.y + image.getHeight() / 2.0;
        double otherTop = other.position.y - other.image.getHeight() / 2.0;
        
        return Math.abs(thisBottom - otherTop) < tolerance;
    }
    
    /**
     * Snap this object to stand on top of a platform
     */
    public void snapToPlatform(List<Platform> platforms) {
        double objectBottom = position.y + image.getHeight() / 2.0;
    
        Platform candidate = platforms.stream()
            .filter(this::isHorizontallyOverlapping)
            .filter(p -> {
                double top = p.getY() - p.getImage().getHeight() / 2.0;
                return top >= objectBottom;
            })
            .min((p1, p2) -> {
                double top1 = p1.getY() - p1.getImage().getHeight() / 2.0;
                double top2 = p2.getY() - p2.getImage().getHeight() / 2.0;
                return Double.compare(top1, top2);
            })
            .orElse(null);
    
        if (candidate != null) {
            double top = candidate.getY() - candidate.getImage().getHeight() / 2.0;
            position = new Point(position.x, top - image.getHeight() / 2.0);
        } else {
            // No platform found below, check if standing on any platform
            boolean standing = platforms.stream().anyMatch(p -> 
                isHorizontallyOverlapping(p) && isCloseToTopOf(p, 5.0));
                
            if (!standing) {
                // Hook method for subclasses to handle no platform situation
                handleNoPlatformBelow(platforms);
            }
        }
    }
    
    /**
     * Hook method that subclasses can override to handle the case when
     * no platform is found below this object. Default implementation does nothing.
     */
    protected void handleNoPlatformBelow(List<Platform> platforms) {
        // Default implementation does nothing
    }
    
    /**
     * Fast AABB collision detection with margins
     * 
     * @param other The other GameObject to check collision with
     * @param xMargin Horizontal margin to reduce collision box by (from each side)
     * @param yMargin Vertical margin to reduce collision box by (from each side)
     * @return true if the objects intersect
     */
    public boolean intersects(GameObject other, double xMargin, double yMargin) {
        // Calculate object boundaries with margins
        double thisHalfWidth = image.getWidth() / 2.0 - xMargin;
        double thisHalfHeight = image.getHeight() / 2.0 - yMargin;
        double otherHalfWidth = other.getImage().getWidth() / 2.0 - xMargin;
        double otherHalfHeight = other.getImage().getHeight() / 2.0 - yMargin;
        
        return Math.abs(position.x - other.getX()) < (thisHalfWidth + otherHalfWidth)
            && Math.abs(position.y - other.getY()) < (thisHalfHeight + otherHalfHeight);
    }
    
    /**
     * Gets bounds array in format [left, top, right, bottom]
     */
    public double[] getBoundsArray() {
        double halfWidth = image.getWidth() / 2.0;
        double halfHeight = image.getHeight() / 2.0;
        return new double[] {
            position.x - halfWidth,  // left
            position.y - halfHeight, // top
            position.x + halfWidth,  // right
            position.y + halfHeight  // bottom
        };
    }
    
    /**
     * A more comprehensive collision detection that returns the side of collision
     * 
     * @param other The other GameObject to check collision with
     * @return 0=no collision, 1=top, 2=right, 3=bottom, 4=left
     */
    public int getCollisionSide(GameObject other) {
        // Get object bounds
        double[] bounds1 = getBoundsArray();
        double[] bounds2 = other.getBoundsArray();
        
        double left1 = bounds1[0];
        double top1 = bounds1[1];
        double right1 = bounds1[2];
        double bottom1 = bounds1[3];
        
        double left2 = bounds2[0];
        double top2 = bounds2[1];
        double right2 = bounds2[2];
        double bottom2 = bounds2[3];
        
        // Check if objects are actually intersecting
        if (right1 <= left2 || left1 >= right2 || bottom1 <= top2 || top1 >= bottom2) {
            return 0; // No collision
        }
        
        // Calculate overlap on each axis
        double overlapX1 = right1 - left2;
        double overlapX2 = right2 - left1;
        double overlapY1 = bottom1 - top2;
        double overlapY2 = bottom2 - top1;
        
        // Find minimum overlap
        double minOverlapX = Math.min(overlapX1, overlapX2);
        double minOverlapY = Math.min(overlapY1, overlapY2);
        
        // Determine collision side based on minimum overlap
        if (minOverlapX < minOverlapY) {
            return (overlapX1 < overlapX2) ? 4 : 2; 
        } else {
            return (overlapY1 < overlapY2) ? 1 : 3; 
        }
    }
}
