(

var numRows = 8;
var numCols = 8;

var window, decorator, matrix, buttonMatrix, leds;
var monome;
var player;
var bpm = 105;
var resolution = 4;
var ledBitMaskFuncOn;
var ledBitMaskFuncOff;
var ledTrigger;
var ledTriggerOff;
var rowOneTrigger;
var inentsity = 15;
monome = Monome.new;
matrix = Array2D.new(numRows, numCols);
matrix = Array2D.fromArray(numRows, numCols, { 0.dup(numCols) }.dup(numRows).value.flat);
matrix[0,0] = 1;
monome.action = { |x, y, on|
    //[x, y, on].postln;
    if(y == 0,
        {
            matrix[y, 0] = 0;
            matrix[y, 1] = 0;
            matrix[y, 2] = 0;
            matrix[y, 3] = 0;
            matrix[y, 4] = 0;
            matrix[y, 5] = 0;
            matrix[y, 6] = 0;
            matrix[y, 7] = 0;
            monome.led(0, y, 0);
            monome.led(1, y, 0);
            monome.led(2, y, 0);
            monome.led(3, y, 0);
            monome.led(4, y, 0);
            monome.led(5, y, 0);
            monome.led(6, y, 0);
            monome.led(7, y, 0);
            matrix[y,x] = 1;
            monome.led(x, y, matrix[y,x]);
        },
        {
            if(on == 1,
                {
                    if(matrix[y,x] == 0,
                        {
                            matrix[y,x] = 1;
                            monome.led(x, y, matrix[y,x]);
                        },{
                            matrix[y,x] = 0;
                            monome.led(x, y, matrix[y,x]);
                    });
            });

        }
    );
};

ledBitMaskFuncOn = {
    arg col;
    var bitNum = 255;
    bitNum = bitNum.setBit(0, false);
    col.do{ |item, pos|
        if(item == 1,
            {
                bitNum = bitNum.setBit(pos, true);
            },
            {
                bitNum = bitNum.setBit(pos, false);
            };
        );
    };
    bitNum;
};
ledBitMaskFuncOff = {
    arg col;
    var bitNum = 255;
    bitNum = bitNum.setBit(0, true);
    col.do{ |item, pos|
        if(item == 1,
            {
                bitNum = bitNum.setBit(pos, false);
            },
            {
                bitNum = bitNum.setBit(pos, true);
            };
        );
    };
    bitNum;
};

// ledTrigger = {
//     arg col, bitMaskOff, bitMaskOn;
//     var t = Task({
//         // monome.intensity(5);
//         monome.ledCol(col, 0, 254);
//         (((bpm/60)/resolution)/4).wait;
//         monome.ledCol(col, 0, bitMaskOn);
//         // (((bpm/60)/resolution)/4).wait;
//         // monome.ledCol(col, 0, bitMask);
//         // (((bpm/60)/resolution)/4).wait;
//         // monome.ledCol(col, 0, 0);
//         // (((bpm/60)/resolution)/4).wait;
//         // monome.intensity(15);
//     });
//     t.start;
// };

ledTrigger = {
    arg col, on;
    var t = Task({
        // monome.intensity(5);
        monome.led(col, 1, 1);
        monome.led(col, 2, 1);
        monome.led(col, 3, 1);
        monome.led(col, 4, 1);
        monome.led(col, 5, 1);
        monome.led(col, 6, 1);
        monome.led(col, 7, 1);
        (((bpm/60)/resolution)/4).wait;
        monome.led(col, 1, on[1]);
        monome.led(col, 2, on[2]);
        monome.led(col, 3, on[3]);
        monome.led(col, 4, on[4]);
        monome.led(col, 5, on[5]);
        monome.led(col, 6, on[6]);
        monome.led(col, 7, on[7]);

    });
    t.start;
};

ledTriggerOff = {
    arg col, on;
    monome.led(col, 1, on[1]);
    monome.led(col, 2, on[2]);
    monome.led(col, 3, on[3]);
    monome.led(col, 4, on[4]);
    monome.led(col, 5, on[5]);
    monome.led(col, 6, on[6]);
    monome.led(col, 7, on[7]);
};

rowOneTrigger = {
    arg row;
    t = Task({
        monome.led(0, 0, row[0]);
        monome.led(1, 0, row[1]);
        monome.led(2, 0, row[2]);
        monome.led(3, 0, row[3]);
        monome.led(4, 0, row[4]);
        monome.led(5, 0, row[5]);
        monome.led(6, 0, row[6]);
        monome.led(7, 0, row[7]);
        (((bpm/60)/resolution)/4).wait;
        monome.ledRow(0, 0, 0);
        (((bpm/60)/resolution)/4).wait;
        monome.led(0, 0, row[0]);
        monome.led(1, 0, row[1]);
        monome.led(2, 0, row[2]);
        monome.led(3, 0, row[3]);
        monome.led(4, 0, row[4]);
        monome.led(5, 0, row[5]);
        monome.led(6, 0, row[6]);
        monome.led(7, 0, row[7]);
        (((bpm/60)/resolution)/4).wait;
        monome.ledRow(0, 0, 0);
    });
};

player = Task({
    inf.do{ |counter|
        var col = counter % numCols;
        // main func
        matrix.colAt(col).do{ |item, pos|
            var ipos = (pos - (numRows-1)).abs;
            // ipos.postln;

            // LED func


            //0.25.wait;      // quarter notes
        };

        rowOneTrigger.value(matrix.rowAt(0)).start;
        if(col != 0)
        {
            {
                // var bitNum = ledBitMaskFuncOn.value(matrix.colAt(col-1));
                // var bitNum2 = ledBitMaskFuncOn.value(matrix.colAt(col));
                // var bitNum3 = ledBitMaskFuncOff.value(matrix.colAt(col));
                // monome.ledCol(col, 0, bitNum2);
                // ledTrigger.value(col, bitNum3, bitNum2);
                //
                // // monome.ledCol(col, 0, 254);
                // // ledTrigger.value(col, 254);
                // monome.ledCol(col-1, 0, bitNum);

                // monome.led(col, 1, 1);
                // monome.led(col, 2, 1);
                // monome.led(col, 3, 1);
                // monome.led(col, 4, 1);
                // monome.led(col, 5, 1);
                // monome.led(col, 6, 1);
                // monome.led(col, 7, 1);
                //
                //
                // monome.led(col-1, 1, 0);
                // monome.led(col-1, 2, 0);
                // monome.led(col-1, 3, 0);
                // monome.led(col-1, 4, 0);
                // monome.led(col-1, 5, 0);
                // monome.led(col-1, 6, 0);
                // monome.led(col-1, 7, 0);

                ledTrigger.value(col, matrix.colAt(col));
                ledTriggerOff.value(col-1, matrix.colAt(col-1));
            }.defer;

        }{
            {
                // var bitNum = ledBitMaskFuncOn.value(matrix.colAt(numCols-1));
                // var bitNum2 = ledBitMaskFuncOn.value(matrix.colAt(col));
                // var bitNum3 = ledBitMaskFuncOff.value(matrix.colAt(col));
                //
                // monome.ledCol(col, 0, bitNum2);
                // ledTrigger.value(col, bitNum3, bitNum2);
                // monome.ledCol(numCols-1, 0, bitNum);

                // monome.led(col, 1, 1);
                // monome.led(col, 2, 1);
                // monome.led(col, 3, 1);
                // monome.led(col, 4, 1);
                // monome.led(col, 5, 1);
                // monome.led(col, 6, 1);
                // monome.led(col, 7, 1);
                //
                //
                // monome.led(numCols-1, 1, 0);
                // monome.led(numCols-1, 2, 0);
                // monome.led(numCols-1, 3, 0);
                // monome.led(numCols-1, 4, 0);
                // monome.led(numCols-1, 5, 0);
                // monome.led(numCols-1, 6, 0);
                // monome.led(numCols-1, 7, 0);

                ledTrigger.value(col, matrix.colAt(col));
                ledTriggerOff.value(numCols-1, matrix.colAt(numCols-1));
            }.defer;
        };
        ((bpm/60)/resolution).wait;

    }
});

// player.start;
CmdPeriod.removeAll;
CmdPeriod.add({player.stop; monome.clear;});
// ledFunc.value(1, 255);
player.start;
)

