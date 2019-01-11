package com.instamaven.app.utils;

public class Plural {

    public static String word(int number, String[] wordsArray) {
        String word;
        number %= 100;
        if (number >= 11 && number <= 19) {
            word = wordsArray[2];
        } else {
            switch (number % 10) {
                case 1:
                    if (Math.floor(number / 10) > 0) {
                        if (wordsArray.length == 4) {
                            word = wordsArray[3];
                        } else {
                            word = wordsArray[2];
                        }
                    } else {
                        word = wordsArray[0];
                    }
                    break;
                case 2:
                case 3:
                case 4:
                    word = wordsArray[1];
                    break;
                default:
                    word = wordsArray[2];
                    break;
            }
        }

        return word;
    }
}
