package andydean.opencvcamera;

import android.util.Pair;

import org.opencv.core.Point;
@SuppressWarnings("WeakerAccess")

/**
 * Created by Andy on 21/12/2016.
 */
public class Line {

    //Coords of start and end points
    public Point start;
    public Point end;
    public double dy;
    public double dx;

    //angle rounded when creating the line so they can be grouped later
    public double angle;
    //Equation of line y = mx + c, m calculated from the rounded angle
    public Double m;
    public Double c;

    public Line(Point p1, Point p2, double angle) {
        setStartAndEnd(p1, p2);
        this.angle = angle;
        setAngle();

    }

    public Line(Point p1, Point p2, int anglePrecision) {

        setStartAndEnd(p1, p2);

        dx = end.x - start.x;
        dy = end.y - start.y;
        double grad = dy / dx;

        double angleRaw = Math.toDegrees(Math.atan(grad));
        this.angle = anglePrecision * (Math.round(angleRaw / anglePrecision));

        setAngle();
    }

    public void setStartAndEnd(Point p1, Point p2){
        if(p1.x == p2.x){
            //If the line is vertical then the start point is the lowest y
            double y1 = Math.min(p1.y, p2.y);
            start = p1.x == y1 ? p1 : p2;
            end = p1.x == y1 ? p2 : p1;
        }else{
            //Make sure the pairs are ordered correctly for later analysis with x1,y1 being the point with the lowest x
            double x1 = Math.min(p1.x, p2.x);
            start = p1.x == x1 ? p1 : p2;
            end = p1.x == x1 ? p2 : p1;
        }
    }

    private void setAngle(){
        if(Math.abs(angle) == 90){
            //Line is vertical then make sure the angle is positive
            this.angle = 90;
            this.m = Double.POSITIVE_INFINITY;
            this.c = Double.POSITIVE_INFINITY;
        }else {
            this.m = Math.tan(Math.toRadians(angle));
            this.c = calcYIntersect(start.x, start.y, this.m);
        }
    }

    public double calcYIntersect(double x, double y, double m){
     return y - (m*x);
    }

    public static Pair<Double, Double> findEqnOfNormal(Point xy, Double m){
        if(m.equals(Double.POSITIVE_INFINITY)){
            //Line is vertical
            return new Pair<> (0.0, xy.y);
        }else if(m == 0){
            //Line is horizontal
            return new Pair<>(Double.POSITIVE_INFINITY, xy.x);
        }
        double mNorm = -1/m;
        double cNorm = xy.y - (mNorm*xy.x);
        return new Pair<>(mNorm, cNorm);

    }

    /**
     * To find the perpendicular distance between two vectors
     * Need to find the equation for the normal of vector 1
     * Find the intersect between this normal and the second vector
     * Calculate distance between the points where the normal crosses each vector
     * @param v1
     * @param v2
     * @return distance
     */
    public static double findPerpendicularDistance(Line v1, Line v2) {
        //If line is vertical
        if(v1.m.equals(Double.POSITIVE_INFINITY) ){
            return Math.abs(v1.start.x-v2.start.x);
        }else if(Math.abs(v1.angle) == 0){
            //If line is horizontal i.e. m = +- 0
            return Math.abs(v1.start.y - v2.start.y);
        }
        Pair<Double, Double> normEq = findEqnOfNormal(v1.start, v1.m);
        Point norm2IntersectV2 = findIntersect(v2.m, v2.c, v2.start, normEq.first, normEq.second, v1.start);
        //The perpendicular line has points v1.x1, v1.y1 and the pair of points returned by findIntersect (with v2)
        if (norm2IntersectV2 != null){
            double dx = norm2IntersectV2.x - v1.start.x;
            double dy = norm2IntersectV2.y - v1.start.y;
            return Math.sqrt((dx * dx) + (dy * dy));
        }
        return 0;
    }

    /**
     * Finds the intersect using the gradient, intersect and a point of each line
     * @param m1
     * @param c1
     * @param a1
     * @param m2
     * @param c2
     * @param a2
     * @return
     */
    public static Point findIntersect(Double m1, Double c1, Point a1, Double m2, Double c2, Point a2) {
        if (m1.equals(m2)){
            //lines are parallel
            return null;
        }
        if(m1.equals(Double.POSITIVE_INFINITY)){
            //line 1 is vertical
            return new Point(a1.x, c2);
        }else if(m1 == 0){
            //line 1 is horizontal
            return new Point(a2.x, c1);
        }
        Double x = (c2-c1)/(m1-m2);
        Double y = (m1*x) + c1;
        return new Point(x,y);
    }

    public static double calcDistBetweenPoints(Point a, Point b){
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt((dx * dx) + (dy * dy));
    }

    public static Point findMidPoint(Point a, Point b){
        double midX = (a.x + b.x)/2;
        double midY = (a.y + b.y)/2;
        return new Point(midX, midY);
    }


}
