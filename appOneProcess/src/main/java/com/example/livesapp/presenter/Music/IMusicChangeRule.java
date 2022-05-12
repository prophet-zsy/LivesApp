package com.example.livesapp.presenter.Music;


/**
 * p层给view层的调用接口
 */

public interface IMusicChangeRule {
    enum PLAY_RULE {
        SEQUENTIALLY(0), SINGLE_LOOP(1), RANDOM(2);
        int value;
        PLAY_RULE(int val) {
            this.value = val;
        }
        static PLAY_RULE parseInt(int val) {
            switch (val) {
                case 0:
                    return SEQUENTIALLY;
                case 1:
                    return SINGLE_LOOP;
                case 2:
                    return RANDOM;
            }
            return null;
        }
    }

    PLAY_RULE getPlayRule();
    void setPlayRule(PLAY_RULE playRule);
}
