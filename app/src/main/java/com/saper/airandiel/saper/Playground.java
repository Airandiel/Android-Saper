package com.saper.airandiel.saper;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.button;
import static android.R.attr.color;
import static android.R.attr.defaultValue;
import static android.R.attr.editTextBackground;
import static android.R.attr.numColumns;
import static android.R.attr.width;
import static android.R.attr.x;
import static android.R.attr.y;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static java.security.AccessController.getContext;

public class Playground extends AppCompatActivity {
    final Handler h = new Handler();
    LinearLayout WHOLE_LAYOUT;
    LinearLayout upperMenu;
    RelativeLayout playground;
    float scale;
    int[][][] array;
    /**
     * the most important array, localisation of mines and counting neighbors
     */
    int playgroundWidth = 10;
    int playgroundHigh = 10;
    int numberOfMines = 20;
    int minesLeft = numberOfMines;
    int indicatedSquares = 0;
    int secondCounter = 0;
    /**
     * First click determine where will be empty square for sure
     */
    int YFirstClick = 0;
    int XFirstClick = 0;
    TextView lose;
    TextView win;
    TextView textMinesLeft;
    TextView textTime;
    boolean firstClick = true;
    boolean endGame = false;
    boolean settingsIsClicked = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        upperMenu = new LinearLayout(this);
        upperMenu.setOrientation(LinearLayout.HORIZONTAL);
        WHOLE_LAYOUT = new LinearLayout(this);
        WHOLE_LAYOUT.setOrientation(LinearLayout.VERTICAL);
        playground = new RelativeLayout(this);
        scale = this.getResources().getDisplayMetrics().density;
        WHOLE_LAYOUT.setPadding((int)(scale*16+0.5f),(int)(scale*16+0.5f),(int)(scale*16+0.5f),(int)(scale*16+0.5f));
        showMenu();
        //prepareGame();

        setContentView(WHOLE_LAYOUT);
    }


    /**
     * Prepare game
     */
    public void prepareGame() {
        WHOLE_LAYOUT.removeAllViews();
        /**initialise values of variables*/
        minesLeft = numberOfMines;
        indicatedSquares = 0;
        secondCounter = 0;
        YFirstClick = 0;
        XFirstClick = 0;

        firstClick = true;
        endGame = false;

        /**timer*/
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textTime = new TextView(this);
        textTime.setLayoutParams(layoutParams);
        textTime.setId(100 * 100 + 100);
        final int delay = 1000; //milliseconds

        h.postDelayed(new Runnable() {
            public void run() {
                //do something
                if (!endGame) {
                    secondCounter++;
                    TextView temp = (TextView) findViewById(100 * 100 + 100);
                    if (secondCounter / 60 < 10) {
                        if (secondCounter % 60 < 10) {
                            temp.setText(" " + (int) secondCounter / 60 + ":0" + secondCounter % 60);
                        } else if (secondCounter % 60 >= 10) {
                            temp.setText("0" + (int) secondCounter / 60 + ":" + secondCounter % 60);
                        }
                    } else {
                        if (secondCounter % 60 < 10) {
                            temp.setText(" " + (int) secondCounter / 60 + ":0" + secondCounter % 60);
                        } else if (secondCounter % 60 >= 10) {
                            temp.setText(" " + (int) secondCounter / 60 + ":" + secondCounter % 60);
                        }
                    }
                }

                h.postDelayed(this, delay);
            }
        }, delay);


        lose = new TextView(this);
        lose.setVisibility(View.INVISIBLE);
        lose.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        lose.setText("You LOSE!");
        lose.setGravity(Gravity.CENTER);
        lose.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);

        win = new TextView(this);
        win.setVisibility(View.INVISIBLE);
        win.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        win.setText("You WIN!");
        win.setGravity(Gravity.CENTER);
        win.setTextColor(Color.RED);
        win.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        //firstGeneration();

        randomise();//create mines
        createPlayground();
        playground.addView(lose);
        playground.addView(win);

        Button menuButton = new Button(this);
        menuButton.setText("Menu");
        menuButton.setLayoutParams(layoutParams);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        TextView textAboutMines = new TextView(this);
        textAboutMines.setText("Mines left: ");
        textAboutMines.setLayoutParams(layoutParams);

        TextView textAboutTime = new TextView(this);
        textAboutTime.setText("Time: ");
        textAboutTime.setLayoutParams(layoutParams);

        textMinesLeft = new TextView(this);
        textMinesLeft.setText("" + minesLeft);
        textMinesLeft.setLayoutParams(layoutParams);

        //textTime.setText("0");
        upperMenu.addView(textAboutMines);
        upperMenu.addView(textMinesLeft);
        upperMenu.addView(menuButton);
        upperMenu.addView(textAboutTime);
        upperMenu.addView(textTime);
        WHOLE_LAYOUT.addView(upperMenu);
        WHOLE_LAYOUT.addView(playground);

    }

    /**
     * Making playground
     */
    public void createPlayground() {

        int pixels = (int) (48 * scale + 0.5f);

        createTextViews();
        ///buttons


        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < playgroundHigh; i++) {
            final LinearLayout row = new LinearLayout(this);
            //pixels = (int) (48 * scale + 0.5f);
            //row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < playgroundWidth; j++) {
                Button btnTag = new Button(this);
                pixels = (int) (48 * scale + 0.5f);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(pixels, pixels, 1);
                //pixels = (int) (5 * scale + 0.8f);

                btnTag.setBackgroundResource(R.drawable.defaultbackground);
                //layoutParams.setMargins(-pixels + (int) (2 * scale + 0.8f), -pixels, -pixels + (int) (2 * scale + 0.8f), -pixels);
                //btnTag.setTextAlignment();
                btnTag.setMinHeight(0);
                btnTag.setMinWidth(0);
                btnTag.setLayoutParams(layoutParams);
                //btnTag.setPadding(0,0,0,0);
                //btnTag.setText("" + (j + 1 + (i * playgroundHigh)));
                btnTag.setId(i * 100 + j);

               /* if (i == XFirstClick && j == YFirstClick) {
                    btnTag.setVisibility(View.INVISIBLE);
                    array[i][j][1]=1;
                    //showZeroButtons(i,j);
                }*/

                btnTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int buttonID = v.getId();
                        int x = buttonID / 100;
                        int y = buttonID % 100;
                        if (firstClick) {
                            firstClick = false;
                            XFirstClick = x;
                            YFirstClick = y;
                            randomise();
                            showZeroButtons(x, y);
                            for (int k = 0; k < playgroundHigh; k++) {
                                for (int t = 0; t < playgroundWidth; t++) {
                                    TextView temp = (TextView) findViewById(k * 100 + t + 1000);
                                    //temp.setText(""+array[k][t][0]);
                                    temp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                                    if (array[k][t][0] != 0) {
                                        temp.setText("" + array[k][t][0]);
                                        if (array[k][t][0] == 1) {
                                            temp.setTextColor(Color.BLUE);
                                        } else if (array[k][t][0] == 2) {
                                            temp.setTextColor(Color.GREEN);
                                        } else if (array[k][t][0] == 3) {
                                            temp.setTextColor(Color.RED);
                                        } else if (array[k][t][0] == 4) {
                                            temp.setTextColor(Color.rgb(160, 8, 80));
                                        } else if (array[k][t][0] == 5) {
                                            temp.setTextColor(Color.rgb(190, 100, 100));
                                        } else if (array[k][t][0] == 6) {
                                            temp.setTextColor(Color.rgb(210, 110, 120));
                                        } else if (array[k][t][0] == 7) {
                                            temp.setTextColor(Color.rgb(255, 120, 140));
                                        } else if (array[k][t][0] == 8) {
                                            temp.setTextColor(Color.rgb(255, 200, 160));
                                        }
                                    } else {
                                        temp.setBackgroundColor(Color.rgb(230, 230, 250));
                                    }
                                }
                            }
                            indicatedSquares++;
                        } else {
                            if (array[x][y][1] != 1) {
                                v.setVisibility(View.INVISIBLE);
                                indicatedSquares++;


                                array[x][y][1] = 1;
                                if (array[x][y][0] == 0) {
                                    showZeroButtons(x, y);
                                }

                                if (array[x][y][0] == 10) {

                                    playground.removeAllViews();
                                    lose.setVisibility(View.VISIBLE);

                                    playground.addView(lose);
                                }
                            }

                        }
                        if (indicatedSquares == playgroundHigh * playgroundWidth + 1 && minesLeft == 0) {
                            playground.removeAllViews();
                            win.setVisibility(View.VISIBLE);
                            playground.addView(win);
                        }
                        //row.removeView(v);
                    }
                });
                btnTag.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int buttonID = v.getId();
                        if (array[buttonID / 100][buttonID % 100][1] != 1) {
                            array[buttonID / 100][buttonID % 100][1] = 1;
                            v.setBackgroundResource(R.drawable.flag);
                            minesLeft--;
                            textMinesLeft.setText("" + minesLeft);
                            indicatedSquares++;
                        } else {
                            v.setBackgroundResource(R.drawable.defaultbackground);
                            minesLeft++;
                            textMinesLeft.setText("" + minesLeft);
                            indicatedSquares--;
                            array[buttonID / 100][buttonID % 100][1] = 0;
                        }
                        if (indicatedSquares == playgroundHigh * playgroundWidth + 1 && minesLeft == 0) {
                            playground.removeAllViews();
                            win.setVisibility(View.VISIBLE);
                            playground.addView(win);
                        }
                        return true;
                    }
                });
                row.addView(btnTag);
            }
            buttons.addView(row);
        }
        //showZeroButtons(XFirstClick,YFirstClick);
       /* for(int k=XFirstClick-1;k<XFirstClick+2;k++){
            for(int t=YFirstClick-1;t<YFirstClick+2;t++){
                if(k!=XFirstClick||t!=YFirstClick){
                    TextView temp = (TextView) findViewById(k*100+t);
                    temp.setVisibility(View.INVISIBLE);
                }
            }
        }*/
        playground.addView(buttons);
    }


    private void createTextViews() {
        int pixels;
        //textViews

        LinearLayout textViews = new LinearLayout(this);
        textViews.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < playgroundHigh; i++) {
            final LinearLayout row = new LinearLayout(this);
            pixels = (int) (48 * scale + 0.5f);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < playgroundWidth; j++) {
                TextView textViewTag = new TextView(this);

                pixels = (int) (48 * scale + 0.5f);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(pixels, pixels, 1);
                //pixels = (int) (3 * scale + 0.5f);
                //layoutParams.setMargins(-pixels, -pixels, -pixels, -pixels);
                //textViewTag.setBackgroundColor(Color.RED);
                //textViewTag.setPadding(30,30,30,30);
                textViewTag.setLayoutParams(layoutParams);
                textViewTag.setId(i * 100 + j + 1000);
                textViewTag.setGravity(Gravity.CENTER);
                ;
                //textViewTag.setId((j + 1 + (i * playgroundHigh)));
                row.addView(textViewTag);
            }
            textViews.addView(row);
        }
        playground.addView(textViews);
    }

    /**
     * Randomise mines and initialise array
     */
    private void randomise() {
        array = new int[playgroundHigh][playgroundWidth][2];
        for (int i = 0; i < numberOfMines; i++) {
            Random generator = new Random();
            int x = generator.nextInt(playgroundHigh); //number of row

            generator = new Random();
            int y = generator.nextInt(playgroundWidth); //number of column
            if ((x >= (XFirstClick - 1)) && (x <= (XFirstClick + 1)) && y >= YFirstClick - 1 && y <= YFirstClick + 1) {
                i = i - 1;
            } else if (array[x][y][0] == 10) {
                i = i - 1;
            } else {
                array[x][y][0] = 10;
            }
        }

        //count neighbors
        for (int i = 0; i < playgroundHigh; i++) {
            for (int j = 0; j < playgroundWidth; j++) {
                if (array[i][j][0] != 10) {
                    array[i][j][0] = numberOfNeighbors(i, j);
                }
            }
        }
    }

    private void showZeroButtons(int x, int y) {
        for (int k = x - 1; k < x + 2; k++) {
            for (int t = y - 1; t < y + 2; t++) {
                if (k >= 0 && t >= 0 && k < playgroundHigh && t < playgroundWidth) {
                    if (array[k][t][1] != 1) {
                        Button temp = (Button) findViewById(k * 100 + t);
                        temp.setVisibility(View.INVISIBLE);
                        indicatedSquares++;
                        //textTime.setText("" + indicatedSquares);
                        array[k][t][1] = 1;
                        if (array[k][t][0] == 0) {
                            showZeroButtons(k, t);
                        }
                    }
                }
            }
        }
    }

    private int numberOfNeighbors(int i, int j) {
        int numberOf = 0;
        if ((i == 0) && (j == 0)) { //Lewy, górny róg
            if (array[i + 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i][j + 1][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j + 1][0] == 10) {
                numberOf++;
            }
        } else if ((i == 0) && (j == (playgroundWidth - 1))) { //Prawy, górny róg
            if (array[i + 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j - 1][0] == 10) {
                numberOf++;
            }
        } else if ((i == (playgroundHigh - 1)) && (j == 0)) { //Lewy, dolny róg
            if (array[i - 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i][j + 1][0] == 10) {
                numberOf++;
            }
            if (array[i - 1][j + 1][0] == 10) {
                numberOf++;
            }
        } else if ((i == (playgroundHigh - 1)) && (j == (playgroundWidth - 1))) {//prawy, dolny róg
            if (array[i - 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i - 1][j - 1][0] == 10) {
                numberOf++;
            }
        } else if (i == 0) {// górna ściana
            if (array[i][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i][j + 1][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j + 1][0] == 10) {
                numberOf++;
            }
        } else if (i == (playgroundHigh - 1)) {// dolna ściana
            if (array[i][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i][j + 1][0] == 10) {
                numberOf++;
            }
            if (array[i - 1][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i - 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i - 1][j + 1][0] == 10) {
                numberOf++;
            }
        } else if (j == 0) { // lewa ściana
            if (array[i - 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i - 1][j + 1][0] == 10) {
                numberOf++;
            }
            if (array[i][j + 1][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j + 1][0] == 10) {
                numberOf++;
            }
        } else if (j == (playgroundWidth - 1)) { //prawa ściana
            if (array[i - 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i - 1][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j - 1][0] == 10) {
                numberOf++;
            }
        } else { //wewnątrz
            if (array[i - 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j][0] == 10) {
                numberOf++;
            }
            if (array[i][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i][j + 1][0] == 10) {
                numberOf++;
            }
            if (array[i - 1][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i - 1][j + 1][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j - 1][0] == 10) {
                numberOf++;
            }
            if (array[i + 1][j + 1][0] == 10) {
                numberOf++;
            }
        }
        //document.write(numberOf);
        return numberOf;
    }

    private void showMenu() {
        h.removeCallbacksAndMessages(null);
        secondCounter = 0;
        upperMenu.removeAllViews();
        playground.removeAllViews();
        WHOLE_LAYOUT.removeAllViews();
        settingsIsClicked = false;
        TextView minesweeper = new TextView(this);
        Button Play = new Button(this);
        Button Settings = new Button(this);
        Button Exit = new Button(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 2;
        minesweeper.setLayoutParams(layoutParams);
        minesweeper.setText("MINESWEEPER");
        minesweeper.setGravity(Gravity.CENTER);
        minesweeper.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        layoutParams.weight = 1;


        Play.setLayoutParams(layoutParams);
        Play.setText("Play");
        Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareGame();

            }
        });

        Settings.setLayoutParams(layoutParams);
        Settings.setText("Settings");
        final LinearLayout bigSettings = new LinearLayout(this);
        bigSettings.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout first = new LinearLayout(this);
        first.setOrientation(LinearLayout.HORIZONTAL);

        final LinearLayout second = new LinearLayout(this);
        second.setOrientation(LinearLayout.HORIZONTAL);

        final LinearLayout third = new LinearLayout(this);
        third.setOrientation(LinearLayout.HORIZONTAL);

        TextView TextHighOfPlayground = new TextView(this);
        TextView TextWidthOfPlayground = new TextView(this);
        TextView TextSetNumberOfMines = new TextView(this);

        TextView EditHighOfPlayground = new TextView(this);
        TextView EditWidthOfPlayground = new TextView(this);
        TextView EditNumberOfMines = new TextView(this);

        Button button1 = new Button(this);
        Button button2 = new Button(this);
        Button button3 = new Button(this);
        Button button4 = new Button(this);
        Button button5 = new Button(this);
        Button button6 = new Button(this);

        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        TextHighOfPlayground.setLayoutParams(layoutParams);
        TextHighOfPlayground.setText("High of playground");
        TextHighOfPlayground.setGravity(Gravity.CENTER);
        TextWidthOfPlayground.setLayoutParams(layoutParams);
        TextWidthOfPlayground.setText("Width of playground");
        TextWidthOfPlayground.setGravity(Gravity.CENTER);
        TextSetNumberOfMines.setLayoutParams(layoutParams);
        TextSetNumberOfMines.setText("Number of mines");
        TextSetNumberOfMines.setGravity(Gravity.CENTER);

        EditHighOfPlayground.setLayoutParams(layoutParams);
        EditHighOfPlayground.setText("" + playgroundHigh);
        EditHighOfPlayground.setId(1 + 9000);
        EditHighOfPlayground.setGravity(Gravity.CENTER);
        EditWidthOfPlayground.setLayoutParams(layoutParams);
        EditWidthOfPlayground.setText("" + playgroundWidth);
        EditWidthOfPlayground.setId(2 + 9000);
        EditWidthOfPlayground.setGravity(Gravity.CENTER);
        EditNumberOfMines.setLayoutParams(layoutParams);
        EditNumberOfMines.setText("" + numberOfMines);
        EditNumberOfMines.setId(3 + 9000);
        EditNumberOfMines.setGravity(Gravity.CENTER);
        //layoutParams.width=LinearLayout.LayoutParams.WRAP_CONTENT;
        //layoutParams.height=(int) (48 * scale + 0.5f);
        button1.setLayoutParams(layoutParams);
        button1.setText("-");
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView temp = (TextView) findViewById(9000 + 1);
                playgroundHigh--;
                if (playgroundHigh < 0) {
                    playgroundHigh = 0;
                }
                if (numberOfMines > (playgroundWidth * playgroundHigh) / 2) {
                    numberOfMines = (playgroundWidth * playgroundHigh) / 2;
                    TextView tempor = (TextView) findViewById(9000 + 3);
                    tempor.setText("" + numberOfMines);
                }
                temp.setText("" + playgroundHigh);
            }
        });
        button2.setLayoutParams(layoutParams);
        button2.setText("+");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView temp = (TextView) findViewById(9000 + 1);
                playgroundHigh++;

                if (numberOfMines > (playgroundWidth * playgroundHigh) / 2) {
                    numberOfMines = (playgroundWidth * playgroundHigh) / 2;
                    TextView tempor = (TextView) findViewById(9000 + 3);
                    tempor.setText("" + numberOfMines);
                }
                temp.setText("" + playgroundHigh);
            }
        });
        button3.setLayoutParams(layoutParams);
        button3.setText("-");
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView temp = (TextView) findViewById(9000 + 2);
                playgroundWidth--;
                if (numberOfMines > (playgroundWidth * playgroundHigh) / 2) {
                    numberOfMines = (playgroundWidth * playgroundHigh) / 2;
                    TextView tempor = (TextView) findViewById(9000 + 3);
                    tempor.setText("" + numberOfMines);
                }
                temp.setText("" + playgroundWidth);
            }
        });
        button4.setLayoutParams(layoutParams);
        button4.setText("+");
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView temp = (TextView) findViewById(9000 + 2);
                playgroundWidth++;
                if (numberOfMines > (playgroundWidth * playgroundHigh) / 2) {
                    numberOfMines = (playgroundWidth * playgroundHigh) / 2;
                    TextView tempor = (TextView) findViewById(9000 + 3);
                    tempor.setText("" + numberOfMines);
                }
                temp.setText("" + playgroundWidth);
            }
        });
        button5.setLayoutParams(layoutParams);
        button5.setText("-");
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView temp = (TextView) findViewById(9000 + 3);
                numberOfMines--;
                if (numberOfMines < 0) {
                    numberOfMines = 0;
                }
                if (numberOfMines > (playgroundWidth * playgroundHigh) / 2) {
                    numberOfMines = (playgroundWidth * playgroundHigh) / 2;
                }
                temp.setText("" + numberOfMines);
            }
        });
        button6.setLayoutParams(layoutParams);
        button6.setText("+");
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView temp = (TextView) findViewById(9000 + 3);
                numberOfMines++;
                if (numberOfMines > (playgroundWidth * playgroundHigh) / 2) {
                    numberOfMines = (playgroundWidth * playgroundHigh) / 2;
                }
                temp.setText("" + numberOfMines);
            }
        });
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        //layoutParams.height=LinearLayout.LayoutParams.WRAP_CONTENT;


        first.addView(TextHighOfPlayground);
        first.addView(button1);
        first.addView(EditHighOfPlayground);
        first.addView(button2);

        second.addView(TextWidthOfPlayground);
        second.addView(button3);
        second.addView(EditWidthOfPlayground);
        second.addView(button4);

        third.addView(TextSetNumberOfMines);
        third.addView(button5);
        third.addView(EditNumberOfMines);
        third.addView(button6);

        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!settingsIsClicked) {
                    bigSettings.addView(first);
                    bigSettings.addView(second);
                    bigSettings.addView(third);
                    WHOLE_LAYOUT.addView(bigSettings);
                    settingsIsClicked = true;
                }
            }
        });
        //layoutParams.width=LinearLayout.LayoutParams.MATCH_PARENT;

        Exit.setLayoutParams(layoutParams);
        Exit.setText("Exit");
        Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        WHOLE_LAYOUT.addView(minesweeper);
        WHOLE_LAYOUT.addView(Play);
        WHOLE_LAYOUT.addView(Settings);
        WHOLE_LAYOUT.addView(Exit);
    }
}