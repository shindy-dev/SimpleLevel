package shindy_works.simplelevel;

import processing.core.PApplet;
import processing.core.PGraphics;


// todo: releaseモードでの実行
// todo: センサーの更新頻度を調節したい
// todo: 水準器の角度制限を設けたい90°の次は-90°
// todo: カメラモード 傾き
// todo: 日本語版, 海外版
// todo: 魚, 鳥
// todo: pip状態で縦横切り替え
// todo: タップしたらその時の角度を0とするやつを作りたい


public final class Processing_LEVEL_APP extends PApplet {
    /* モード変更する角度 */
    private final static double change_mode_rad1 = 50 * Math.PI / 180;
    private final static double change_mode_rad2 = change_mode_rad1 + (80 * Math.PI / 180);
    /* グローバル変数 *************************************************************/
    /*androidのセンサー関連*/
    // x軸,y軸,z軸それぞれの方向の加速度
    private static double ax = 0d, ay = 0d, az = 0d;
    private static double[] radXYZ = new double[3], abs_radXYZ = new double[3];
    /* フラグ */
    private static boolean ShowDegree = true, // 角度表示
            Landscape = false, // 横画面
            pipMode = false, // pip
            Hmode = false, Vmode = false, Mmode = true; // モード

    /* サイズ */
    private static int prevWidth, prevHeight;// 更新前のサイズ
    private float half_height;
    private int max_wh,  // 幅、高さの大きい方
            text_size, // 文字サイズ
            line_length; // 水平器使用時に表示する2本の線の長さ

    /* 明るさマネージャー */
    private LightManager lmBgLower, lmBgUpper, lmWater;

    /* オリジナルデザイン */
    private PGraphics ring, valueText, sun, moon;

    /******************************************************************************/

    public static final void setLandscape(boolean isChecked) {
        Landscape = isChecked;
    }

    public static final void setPipMode(boolean isPictureInPictureMode) {
        pipMode = isPictureInPictureMode;
    }

    public static final void setHmode(boolean isChecked) {
        Hmode = isChecked;
        Vmode = Mmode = !isChecked;
    }

    public static final void setVmode(boolean isChecked) {
        Vmode = isChecked;
        Hmode = Mmode = !isChecked;
    }

    public static final void setMmode(boolean isChecked) {
        Mmode = isChecked;
        Hmode = Vmode = !isChecked;
    }

    public static final void setAccValue(float[] values) {
        ax = ax * 0.9d + values[0] * 0.1d;
        ay = ay * 0.9d + values[1] * 0.1d;
        az = az * 0.9d + values[2] * 0.1d;

        if (Landscape)
            radXYZ = new double[]{Math.atan2(ay, ax), Math.atan2(-ay, az), Math.atan2(ax, az)};
        else
            radXYZ = new double[]{-Math.atan2(ax, ay), -Math.atan2(ax, az), Math.atan2(-ay, az)};
        abs_radXYZ = new double[]{Math.abs(radXYZ[0]), Math.abs(radXYZ[1]), Math.abs(radXYZ[2])};
    }

    public final void setShowDegree(boolean isChecked) {
        ShowDegree = !isChecked;
        if (!ShowDegree)
            textSize(text_size >> 1);
        else
            textSize(text_size);
    }

    private void InitAll() {
        final int min_wh = min(width, height);
        InitGlobal(min_wh);
        InitvalueText(min_wh);
        InitRing(min_wh);
        InitSun(min_wh);
        InitMoon(min_wh);
    }

    private void InitGlobal(int min_wh) {
        max_wh = max(width, height);
        line_length = round((float) 5 / 54 * min_wh);
        half_height = height >> 1;
    }

    private void InitvalueText(int min_wh) {
        text_size = round(((float) 5 / 18) * min_wh);
        if (valueText == null)
            valueText = createGraphics(min_wh, text_size, P2D);
        valueText.beginDraw();
        valueText.clear();
        valueText.resize(min_wh, text_size);
        valueText.noStroke();
        valueText.fill(255);
        valueText.textAlign(CENTER, CENTER);
        valueText.textSize(text_size);
        valueText.endDraw();
    }

    private void InitRing(int min_wh) {
        final int ringD = round(((float) 5 / 9) * min_wh);
        if (ring == null)
            ring = createGraphics(ringD + 10, ringD + 10, P2D);
        ring.beginDraw();
        ring.clear();
        ring.resize(ringD + 10, ringD + 10);
        ring.noFill();
        ring.strokeWeight(10);
        ring.stroke(255);
        ring.ellipse((float) (ring.width >> 1), (float) (ring.height >> 1), ringD, ringD);
        ring.endDraw();
    }

    private void InitSun(int min_wh) {
        final int sunD = round(((float) 5 / 9) * min_wh);
        if (sun == null)
            sun = createGraphics(sunD + 10, sunD + 10, P2D);
        sun.beginDraw();
        sun.clear();
        sun.resize(sunD + 10, sunD + 10);
        sun.fill(255, 119, 0);
        sun.noStroke();
        sun.ellipse((float) (sun.width >> 1), (float) (sun.height >> 1), sunD, sunD);
        sun.endDraw();
    }

    private void InitMoon(int min_wh) {
        final int moonD = round(((float) 5 / 9) * min_wh);
        if (moon == null)
            moon = createGraphics(moonD, moonD, P2D);
        moon.beginDraw();
        moon.clear();
        moon.resize(moonD, moonD);
        moon.fill(135, 135, 135);
        moon.noStroke();
        moon.ellipse((float) (moon.width >> 1), (float) (moon.height >> 1), moonD, moonD);
        moon.endDraw();
    }

    public void pre() {
        if (prevWidth != width || prevHeight != height) {
            prevWidth = width;
            prevHeight = height;
            InitAll();
        }
    }

    @Override
    public final void settings() {
        fullScreen(P2D);
        prevWidth = width;
        prevHeight = height;
        text_size = line_length = 0;
        ring = valueText = sun = moon = null;
        lmWater = new LightManager(64, 128, 191, false); // 水平器の床の透過度
        lmBgLower = new LightManager(64, 128, 191, false); // 垂直器の背景透過度(下向き)
        lmBgUpper = new LightManager(191, 128, 64, true); // 垂直器の背景透過度(上向き)
        registerMethod("pre", this);
    }

    @Override
    public final void setup() {
        InitAll();
        noStroke();
    }

    @Override
    public final void draw() {
        if (Mmode) {
            if ((change_mode_rad1 < abs_radXYZ[1] && abs_radXYZ[1] < change_mode_rad2)
                    || (change_mode_rad1 < abs_radXYZ[2] && abs_radXYZ[2] < change_mode_rad2)) {
                vertical_level();
                lmBgUpper.resetLight();
                lmBgLower.resetLight();
            } else {
                horizontal_level();
                lmWater.resetLight();
            }
        } else if (Hmode) {
            horizontal_level();
            lmBgUpper.resetLight();
            lmBgLower.resetLight();
        } else if (Vmode) {
            vertical_level();
            lmWater.resetLight();
        }

    }

    @Override
    public final void onPause() {
        if (pipMode) {
            int min_wh = min(width, height);
            InitGlobal(min_wh);
        } else
            super.onPause();
    }

    /*垂直持ち*/
    private void vertical_level() {
        /* 計算処理 */
        float above_sea_level;
        if ((PI / 4 <= abs_radXYZ[0] && abs_radXYZ[0] <= 3 * PI / 4))
            above_sea_level = (float) (-3 * (max_wh >> 1) * (1 - abs_radXYZ[1] / (HALF_PI)));
        else
            above_sea_level = (float) (-3 * (max_wh >> 1) * (1 - abs_radXYZ[2] / (HALF_PI)));


        long deg_XY;
        if (abs_radXYZ[0] > HALF_PI) {
            if (radXYZ[0] < 0)
                deg_XY = Math.round((-Math.PI - radXYZ[0]) * 180 / Math.PI);
            else
                deg_XY = Math.round((Math.PI - radXYZ[0]) * 180 / Math.PI);
        } else
            deg_XY = Math.round(radXYZ[0] * 180 / Math.PI);

        /* 描画処理 */

        background(255);

        pushMatrix(); // 現在の座標軸を保存
        translate(width >> 1, half_height); // 画面の中心を座標軸に
        rotate((float) -radXYZ[0]); // 座標軸中心に回転

        if (deg_XY == 0)
            fill(lmWater.toLight());
        else
            fill(lmWater.toDark());
        quad(-max_wh, above_sea_level, max_wh, above_sea_level, max_wh, max_wh, -max_wh, max_wh);


        //PGraphics btext = valueText;
        final int btext_halfW = valueText.width >> 1;
        final int btext_halfH = valueText.height >> 1;

        valueText.beginDraw();
        valueText.background(0, 0);
        valueText.fill(255);
        if (ShowDegree) {
            valueText.textSize(text_size);
            valueText.text(deg_XY + "\u00B0", btext_halfW, btext_halfH);
        } else {
            valueText.textSize(text_size >> 1);
            valueText.text(deg_XY + "\u00B0", btext_halfW, btext_halfH - (text_size >> 2));
            if (abs(deg_XY) == 90) {
                if (deg_XY > 0)
                    valueText.text("\u221E", btext_halfW, btext_halfH + (text_size >> 2));
                else
                    valueText.text("-\u221E", btext_halfW, btext_halfH + (text_size >> 2));
            } else
                valueText.text(Math.round(Math.tan(radXYZ[0]) * 100) + "%", btext_halfW, btext_halfH + (text_size >> 2));
        }
        valueText.endDraw();

        blendMode(EXCLUSION);
        image(valueText, -btext_halfW, -btext_halfH);
        popMatrix(); // 保存した座標軸に戻す

        fill(255);
        rect(0, half_height - 2, line_length, 5);
        rect(width - line_length, half_height - 2, line_length, 5);

        blendMode(BLEND);
    }

    /* 水平持ち */
    private void horizontal_level() {

        // xy平面での加速度が 45°～ 135°と 225°～ 315°を示した場合,yz平面の加速度の角度を用いる。
        // それ以外はxz平面の加速度の角度を用いる。
        double[] res;
        if ((PI / 4 <= abs_radXYZ[0] && abs_radXYZ[0] <= 3 * PI / 4))
            res = calc_vertical_level(radXYZ[1], abs_radXYZ[1]).clone();
        else
            res = calc_vertical_level(radXYZ[2], abs_radXYZ[2]).clone();
        final float dist_rings = (float) res[0]; // 2つのボールの距離
        final int deg_YZ_XZ = (int) res[1]; // degree(度数法)
        final int per_YZ_XZ = (int) res[2];


        blendMode(BLEND);
        //PGraphics btext = valueText;
        final int btext_halfW = valueText.width >> 1;
        final int btext_halfH = valueText.height >> 1;

        pushMatrix();
        translate(width >> 1, half_height);
        rotate((float) (-radXYZ[0]));

        int tc = 255;
        if (deg_YZ_XZ == 0) {
            int bg;
            if (abs_radXYZ[1] > change_mode_rad2 || abs_radXYZ[2] > change_mode_rad2)
                bg = lmBgUpper.toDark();
            else
                bg = lmBgLower.toLight();
            background(bg);
            image(ring, (-ring.width >> 1), (-ring.height >> 1));

        } else {
            if (abs_radXYZ[1] > change_mode_rad2 || abs_radXYZ[2] > change_mode_rad2) {
                background(lmBgUpper.toLight());
                image(sun, -(sun.width >> 1), -dist_rings - (sun.height >> 1));
                image(moon, -(moon.width >> 1), dist_rings - (moon.height >> 1));
                tc = 0;
            } else {
                background(lmBgLower.toDark());
                image(moon, -(moon.width >> 1), -dist_rings - (moon.height >> 1));
                image(sun, -(sun.width >> 1), dist_rings - (sun.height >> 1));
            }
        }

        valueText.beginDraw();
        valueText.background(0, 0);
        valueText.fill(tc);
        if (ShowDegree) {
            valueText.textSize(text_size);
            valueText.text(deg_YZ_XZ + "\u00B0", btext_halfW, btext_halfH);
        } else {
            valueText.text(deg_YZ_XZ + "\u00B0", btext_halfW, btext_halfH - (text_size >> 2));
            valueText.textSize(text_size >> 1);
            if (abs(deg_YZ_XZ) == 90) {
                if (deg_YZ_XZ > 0)
                    valueText.text("\u221E", btext_halfW, btext_halfH + (text_size >> 2));
                else
                    valueText.text("-\u221E", btext_halfW, btext_halfH + (text_size >> 2));
            } else
                valueText.text(per_YZ_XZ + "%", btext_halfW, btext_halfH + (text_size >> 2));
        }
        valueText.endDraw();
        image(valueText, -btext_halfW, -btext_halfH);
        popMatrix();
    }

    private double[] calc_vertical_level(double rad, double abs_rad) {
        double dist_rings, deg_YZ_XZ, per_YZ_XZ;
        if (abs_rad > HALF_PI) {
            dist_rings = 2.5 * max_wh * (1 - (abs_rad) / Math.PI);
            if (rad < 0) {
                deg_YZ_XZ = (int) Math.round(((-Math.PI - rad) * 180 / Math.PI));
                per_YZ_XZ = Math.round(Math.tan(-Math.PI - rad) * 100);
            } else {
                deg_YZ_XZ = (int) Math.round(((Math.PI - rad) * 180 / Math.PI));
                per_YZ_XZ = Math.round(Math.tan(Math.PI - rad) * 100);
            }
        } else {
            dist_rings = 2.5 * max_wh * (1 - (1 - (abs_rad / Math.PI)));
            deg_YZ_XZ = (int) Math.round((rad * 180 / Math.PI));
            per_YZ_XZ = Math.round(Math.tan(rad) * 100);
        }
        return new double[]{dist_rings, deg_YZ_XZ, per_YZ_XZ};
    }

    private final class LightManager {
        private final int L_delta = 2;
        private int H, S, L, L_Max;
        private boolean R2D;

        private LightManager(int r, int g, int b, boolean R2D) {
            H = S = L = 0;
            this.R2D = R2D;
            get_hsl(r, g, b);
            L_Max = L;
            setLight();
        }

        private void setLight() {
            if (R2D)
                L = 127;
            else
                L = 0;
        }

        private void resetLight() {
            if (L == 127 || L == 0)
                return;
            setLight();
        }

        private void get_hsl(int r, int g, int b) {
            int rgbMax = max(r, g, b), rgbMin = min(r, g, b);
            int maxminDiff = rgbMax - rgbMin;
            if (rgbMax != rgbMin) {
                if (rgbMax == r) H = round(60 * ((float) (g - b) / maxminDiff));
                if (rgbMax == g) H = round(60 * ((float) (b - r) / maxminDiff)) + 120;
                if (rgbMax == b) H = round(60 * ((float) (r - g) / maxminDiff)) + 240;
            }

            if (H < 0)
                H += 360;


            L = round((float) (rgbMax + rgbMin) / 2);
            if (L <= 127)
                S = round((float) maxminDiff / (rgbMax + rgbMin) * 100);
            else
                S = round((float) maxminDiff / (510 - rgbMax - rgbMin) * 100);
            L = round(((float) L / 255) * 100);
        }

        // メンバ変数のHSL値からRGB値を取得
        private int get_rgb() {
            int r = 0, g = 0, b = 0;
            float max, min;
            if (L <= 49) {
                max = 2.55f * (L + L * ((float) S / 100));
                min = 2.55f * (L - L * ((float) S / 100));
            } else {
                max = 2.55f * (L + (100 - L) * ((float) S / 100));
                min = 2.55f * (L - (100 - L) * ((float) S / 100));
            }
            if (H < 60) {
                r = round(max);
                g = round(min + (max - min) * ((float) H / 60));
                b = round(min);
            } else if (H < 120) {
                r = round(min + (max - min) * ((float) (120 - H) / 60));
                g = round(max);
                b = round(min);
            } else if (H < 180) {
                r = round(min);
                g = round(max);
                b = round(min + (max - min) * ((float) (H - 120) / 60));
            } else if (H < 240) {
                r = round(min);
                g = round(min + (max - min) * ((float) (240 - H) / 60));
                b = round(max);
            } else if (H < 300) {
                r = round(min + (max - min) * ((float) (H - 240) / 60));
                g = round(min);
                b = round(max);
            } else if (H < 360) {
                r = round(max);
                g = round(min);
                b = round(min + (max - min) * ((float) (360 - H) / 60));
            }

            return color(r, g, b);
        }

        private int toDark() {
            if (R2D) {
                if (L > this.L_Max)
                    L -= L_delta;
            } else if (L > 0)
                L -= L_delta;

            return get_rgb();
        }

        private int toLight() {
            if (R2D) {
                if (L < 127)
                    L += L_delta;
            } else if (L < this.L_Max)
                L += L_delta;
            return get_rgb();
        }
    }
}
