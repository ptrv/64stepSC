(

var numRows = 8;
var numCols = 8*8;
var colsOffset = 0;
var loopStart = 4;
var loopLength = 8;

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
var pager = 0;
var calcWaitTime;
var waitTime = (bpm/60)/resolution;

monome = Monome.new;
matrix = Array2D.new(numRows, numCols);
matrix = Array2D.fromArray(numRows, numCols, { 0.dup(numCols) }.dup(numRows).value.flat);
matrix[0,0] = 1;
monome.action = { |x, y, on|
    // [x, y, on].postln;
    var vX = x + (pager * 8);

    // if button pushed in first row
    if(y == 0, {
        8.do { |i|
            matrix[0, i] = 0;
            monome.led(i, 0, 0);
        };
        matrix[y,x] = 1;
        monome.led(x, y, matrix[y,x]);

        // set pager
        pager = x;

        // refresh leds
        (1..7).do { |i|
            8.do { |j|
                var tmpX = (pager * 8) + j;
                monome.led(j, i, matrix[i, tmpX])
            };
        };
    },{
        if(on == 1, {
            if(matrix[y,vX] == 0, {
                matrix[y,vX] = 1;
                monome.led(x, y, matrix[y,vX]);
            },{
                matrix[y,vX] = 0;
                monome.led(x, y, matrix[y,vX]);
            });
        });
    });
};

ledTrigger = {
    arg col, on;
    var t = Task({
        // monome.intensity(5);
        var vColS = (pager*8);
        var vColE = (pager*8 + 8);
        if((col >= vColS) && (col < vColE),{
            (numRows-1).do { |i|
                monome.led(col%8, i+1, 1);
            };
            (waitTime/8).wait;
            (numRows-1).do { |i|
                monome.led(col%8, i+1, on[i+1]);
            };
        });
    });
    t.start;
};

// function to set leds back which are not the timeline
ledTriggerOff = {
    arg col, on;
    var vColS = (pager*8);
    var vColE = (pager*8 + 8);
    if((col >= vColS) && (col < vColE), {
        (numRows-1).do { |i|
            monome.led(col%8, i+1, on[i+1]);
        };
    });
};

rowOneTrigger = {
    arg row;
    t = Task({
        row.do { |item, i|
            monome.led(i, 0, row[i]);
        };
        (waitTime/4).wait;
        monome.ledRow(0, 0, 0);
        (waitTime/4).wait;
        row.do { |item, i|
            monome.led(i, 0, row[i]);
        };
        (waitTime/4).wait;
        monome.ledRow(0, 0, 0);
        (waitTime/4).wait;
        row.do { |item, i|
            monome.led(i, 0, row[i]);
        };
        monome.ledRow(0, 0, 0);
    });
};

player = Task({
    inf.do{ |counter|
        var col = (counter % loopLength) + loopStart;
        var lastCol = loopStart + loopLength;
        var visibleColStart = pager * 8;
        // main func
        matrix.colAt(col).do{ |item, pos|
            // TODO: implement sound playing
        };

        rowOneTrigger.value(matrix.rowAt(0)).start;
        if(col != 0)
        {
            {
                ledTrigger.value(col, matrix.colAt(col));
                ledTriggerOff.value(col-1, matrix.colAt(col-1));
            }.defer;
        }{
            {
                ledTrigger.value(col, matrix.colAt(col));
                ledTriggerOff.value(lastCol-1, matrix.colAt(lastCol-1));
            }.defer;
        };
        ((bpm/60)/resolution).wait;

    }
});

// player.start;
CmdPeriod.removeAll;
CmdPeriod.add({player.stop; monome.clear;});
player.start;
)
