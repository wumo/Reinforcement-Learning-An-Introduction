package wumo.rl.util;

public class Precise {
    public static final double EPS = 1e-8;
    
    public static double sqr(double x) {
        return x * x;
    }
    
    public static int sign(double x) {
        if (x > EPS)
            return 1;
        return x < -EPS ? -1 : 0;
    }
}
