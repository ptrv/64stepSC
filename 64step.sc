SixtyFourStep {

    var <bpm;
    var <>resolution;
    var pages;
    var <>loopLength = 8;
    var <numRows=8;
    var <numCols;
    var <>loopStart = 0;

    var matrix;
    var monome;
    var <player;
    var ledBitMaskFuncOn;
    var ledBitMaskFuncOff;
    var ledTrigger;
    var ledTriggerOff;
    var rowOneTrigger;
    var inentsity = 15;
    var pager = 0;
    var gui;
    var <>samples;
    var window, decorator;
    var sndFileViews;
    var sndLoadButtons;
    var posButtons;
    var pageButtons;
    var posView;


    *new { |bpm=105, resolution=4, pages=2, loopLength=8|
        ^super.newCopyArgs(bpm, resolution, pages, loopLength).init;
    }

    init {
        Server.default.waitForBoot({
            numCols = pages * 8;
            if(loopLength > numCols)
            {
                loopLength = numCols;
                ("loopLength larger than number of columns. Set to number of columns.").warn;
            };
            monome = Monome.new;
            // matrix = Array2D.new(numRows, numCols);
            matrix = Array2D.fromArray(numRows, numCols, { 0.dup(numCols) }.dup(numRows).value.flat);
            matrix[0,0] = 1;

            monome.action = { |x, y, on|
                // [x, y, on].postln;
                var vX = x + (pager * 8);

                // if button pushed in first row
                if(y == 0)
                {
                    if(x < (numCols/8))
                    {
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
                        this.update;
                    };
                }{
                    if(on == 1)
                    {
                        if(matrix[y,vX] == 0)
                        {
                            matrix[y,vX] = 1;
                            monome.led(x, y, matrix[y,vX]);
                        }{
                            matrix[y,vX] = 0;
                            monome.led(x, y, matrix[y,vX]);
                        };
                    };
                };
                this.update;
            };

            ledTrigger = {
                arg col, on;
                var task = Task.new({
                    // monome.intensity(5);
                    var vColS = (pager*8);
                    var vColE = (pager*8 + 8);
                    if((col >= vColS) && (col < vColE))
                    {
                        (numRows-1).do { |i|
                            monome.led(col%8, i+1, 1);
                        };
                        this.waitTime(0.125).wait;
                        (numRows-1).do { |i|
                            monome.led(col%8, i+1, on[i+1]);
                        };
                    };
                });
                task.start;
            };

            // function to set leds back which are not the timeline
            ledTriggerOff = {
                arg col, on;
                var vColS = (pager*8);
                var vColE = (pager*8 + 8);
                if((col >= vColS) && (col < vColE))
                {
                    (numRows-1).do { |i|
                        monome.led(col%8, i+1, on[i+1]);
                    };
                };
            };

            rowOneTrigger = {
                arg row;
                var task = Task({
                    row.do { |item, i|
                        monome.led(i, 0, row[i]);
                    };
                    this.waitTime(0.25).wait;
                    monome.ledRow(0, 0, 0);
                    this.waitTime(0.25).wait;
                    row.do { |item, i|
                        monome.led(i, 0, row[i]);
                    };
                    this.waitTime(0.25).wait;
                    monome.ledRow(0, 0, 0);
                    this.waitTime(0.25).wait;
                    row.do { |item, i|
                        monome.led(i, 0, row[i]);
                    };
                    monome.ledRow(0, 0, 0);
                });
                task;
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
                    this.waitTime.wait;
                }
            });

            samples = [];
            7.do { |i|
                samples.insert(i, SoundFile.new);
            };

            // ------------------------------------------------------------------
            // GUI
            // ------------------------------------------------------------------
            window = Window("SixtyFourStep", Rect(128, 64, 600, 400)).alwaysOnTop_(true);
            decorator = window.addFlowLayout(5@5, 5@5);

            posButtons = [];
            pageButtons = Array.new;
            posView = View(window, 100@40);
            posView.decorator = FlowLayout(posView.bounds,0@0, 0@0);
            8.do {|i|
                var button;

                button = Button(posView, 10@10);
                button.states = [
                    [ "", Color.gray, Color.gray ],
                    [ "", Color.green, Color.green ]
                ];
                pageButtons = pageButtons.add(button);
            };
            pageButtons.size.postln;
            pageButtons[0].value = 1;
            pageButtons[0].doAction;

            posView.decorator.nextLine;
            8.do {|i|
                var button;
                button = Button(posView, 10@10);

                posButton = posButtons.add(button);
            };

            decorator.nextLine;

            sndFileViews = [];
            sndLoadButtons = [];
            7.do { arg i;
                if(i != 0, {decorator.nextLine});
                sndFileViews.insert(i, SoundFileView.new(window, 120@40));
                sndLoadButtons.insert(i, Button.new(window, 30@30)
                    .action_({
                        Dialog.openPanel({
                            arg path;
                            var sndF = SoundFile.new;
                            sndF.openRead(path);
                            samples = samples.put(i, sndF);
                            sndFileViews[i].soundfile = sndF;
                            sndFileViews[i].read(0, sndF.numFrames);
                            sndFileViews[i].refresh;
                        });
                    })
                );
            };

            window.front;
            // ------------------------------------------------------------------
            CmdPeriod.add({this.reset});
            this.player.start;
            this.update;
        });

    }

    loadSample { arg index;


        ^samples.at(index);
    }
    waitTime { arg mult=1;
        ^(((bpm/60)/resolution)*mult);
    }

    start {
        player.start;
    }

    stop {
        player.stop;
    }

    pause {
        player.pause;
    }

    resume {
        player.resume;
    }

    // reset {
    //     player.reset;
    // }

    clear {
        monome.clear;

    }

    reset {
        numRows.do { |i|
            numCols.do { |j|
                matrix[i, j] = 0;
            };
        };
        matrix[0,0] = 1;
        monome.clear;
    }

    free {
        player.stop;
        monome.clear;
    }

    update {
        pageButtons.do {|butt, i|
            {
                butt.value = matrix[0, i];
                butt.doAction;
            }.defer;
        };
    }

}
