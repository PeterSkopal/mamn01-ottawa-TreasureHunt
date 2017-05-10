package com.ottawa.treasurehunt.treasurehunt.utils.game;

import com.ottawa.treasurehunt.treasurehunt.utils.Parser;

/**
 * Created by Skopal on 10/05/17.
 */

public class Main {
    public static void main(String args[]) {
        String json = "{\n" +
                "    \"id\" : 1,\n" +
                "    \"name\" : \"first game\",\n" +
                "    \"description\" : \"This is our test game\",\n" +
                "\n" +
                "    \"position\": {\n" +
                "      \"lat\" : 55.711165,\n" +
                "\t    \"long\" : 13.207776\n" +
                "    },\n" +
                "    \"checkpoints\" : [ {\n" +
                "        \"position\" : {\n" +
                "            \"lat\" : 55.710977,\n" +
                "            \"long\" : 13.208388\n" +
                "        },\n" +
                "        \"minigame\" : null,\n" +
                "        \"quiz\" : [ {\n" +
                "            \"answers\" : [ {\n" +
                "                  \"answer\" : \"190 m\",\n" +
                "                  \"correct\" : true\n" +
                "                }, {\n" +
                "                  \"answer\" : \"160 m\",\n" +
                "                  \"correct\" : false\n" +
                "                }, {\n" +
                "                  \"answer\" : \"220 m\",\n" +
                "                  \"correct\" : false\n" +
                "                }, {\n" +
                "                  \"answer\" : \"260 m\",\n" +
                "                  \"correct\" : false\n" +
                "                } ],\n" +
                "            \"question\" : \"How tall is the Turning Torso?\"\n" +
                "        } ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"position\" : {\n" +
                "            \"lat\" : 55.710802,\n" +
                "            \"long\" : 13.207390\n" +
                "        },\n" +
                "        \"minigame\" : 1,\n" +
                "        \"quiz\" : null\n" +
                "      } ]\n" +
                "  }";

        Game game = Parser.generateGame(json);

        if (game != null) {

        }
    }
}
