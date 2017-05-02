```
games : [ {
    name : string,
    description : string,

    position: {
        long : double,
        lat : double
    },
    checkpoints: [ {
        position: {
            long : double,
            lat : double
        },
        minigame : id,
        quiz : [ {
            answers : [ {
                answer : string,
                correct : bool
                } ],
            question : string
        } ]
    } ]
} ]
```
