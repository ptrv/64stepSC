SoundFile64step : SoundFile {
    var <>selections;

    *new {
        ^super.new.init
    }

    init {
        selections = [];
        64.do {
            selections = selections.add([0, 0]);
        }
    }
}

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
    var <>samples, samplesMenu, muteButtons, muteAllBtn;
    var window, decorator;
    var sndFileView, sndLoadButton, sndFilePopup;
    var sndPropsView, currentRowBtn;
    var posButtons, pageButtons, posView, sndEnvView;
    var goBtn, currentSample;


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
            matrix = Array2D.fromArray(numRows, numCols, 0.dup(numRows*numCols));
            // set current page to 1
            matrix[0,0] = 1;
            // ------------------------------------------------------------------
            // Monome action
            // ------------------------------------------------------------------
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

            // ------------------------------------------------------------------
            // Trigger functions
            // ------------------------------------------------------------------
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

            // ------------------------------------------------------------------
            // player task
            // ------------------------------------------------------------------
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

            // ------------------------------------------------------------------
            // GUI
            // ------------------------------------------------------------------
            window = Window("SixtyFourStep", Rect(128, 64, 640, 200)).alwaysOnTop_(true);
            decorator = window.addFlowLayout(5@5, 5@5);

            // ------------------------------------------------------------------
            posButtons = [];
            pageButtons = Array.new;
            posView = View(window, 100@20);
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
                button.states = [
                    [ "", Color.gray, Color.gray ],
                    [ "", Color.green, Color.green ]
                ];

                posButtons = posButtons.add(button);
            };
            goBtn = Button(window, 20@20);
            goBtn.states = [
                ["Go", Color.white, Color.gray ],
                ["Go", Color.white, Color.green ]
            ];
            goBtn.action = { arg btn;
                if(this.player.isPlaying)
                {
                    this.player.pause;
                    // btn.value_(0);
                    "pause".postln;
                }{
                    this.player.play;
                    // btn.value_(1);
                    "play".postln;
                }
            };

            // ------------------------------------------------------------------
            decorator.nextLine;

            // ------------------------------------------------------------------
            sndPropsView = View(window, 200@150);
            sndPropsView.decorator = FlowLayout(sndPropsView.bounds, 0@0);
            currentRowBtn = Button.new(sndPropsView, 40@40);
            currentRowBtn.states = [
                ["A 1", Color.white, Color.gray],
                ["S 2", Color.white, Color.gray],
                ["D 3", Color.white, Color.gray],
                ["F 4", Color.white, Color.gray],
                ["G 5", Color.white, Color.gray],
                ["H 6", Color.white, Color.gray],
                ["J 7", Color.white, Color.gray]
            ];
            currentRowBtn.action = { arg butt;
                butt.value.postln;
            };

            window.view.keyDownAction = { arg view, char, modifiers, unicode, keycode;
                switch(char,
                    $a, {currentRowBtn.valueAction_(0)},
                    $s, {currentRowBtn.valueAction_(1)},
                    $d, {currentRowBtn.valueAction_(2)},
                    $f, {currentRowBtn.valueAction_(3)},
                    $g, {currentRowBtn.valueAction_(4)},
                    $h, {currentRowBtn.valueAction_(5)},
                    $j, {currentRowBtn.valueAction_(6)}
                );
            };

            samples = [];
            sndLoadButton = Button.new(sndPropsView, 40@40);
            sndLoadButton.states = [["Load", Color.white, Color.gray]];
            sndLoadButton.action = {
                Dialog.openPanel({ arg path;
                    var sndF = SoundFile64step.new;
                    var pn;
                    sndF.openRead(path);
                    pn = PathName(sndF.path);
                    samples = samples.add(sndF);
                    samplesMenu.items = samplesMenu.items.add(pn.fileNameWithoutExtension);
                    currentSample = samples.size-1;
                    samplesMenu.valueAction_(currentSample);
                });
            };

            sndPropsView.decorator.nextLine;
            samplesMenu = PopUpMenu(sndPropsView, 200@20);
            samplesMenu.items = [];
            samplesMenu.action = { arg menu;
                currentSample = menu.value;
                sndFileView.soundfile = samples.at(currentSample);
                sndFileView.read(0, sndFileView.soundfile.numFrames);
                if(samples.at(currentSample).selections.size > 0)
                {
                    sndFileView.setSelection(0, samples.at(currentSample).selections[0]);
                };
                sndFileView.refresh;
            };
            sndPropsView.decorator.nextLine;
            7.do { |i|
                var butt = Button(sndPropsView, 20@20);
                butt.states = [
                    [(""+i), Color.white, Color.grey],
                    [(""+i), Color.white, Color.red]
                ];
                muteButtons = muteButtons.add(butt);
            };
            muteAllBtn = Button(sndPropsView,
                sndPropsView.decorator.indentedRemaining.width@20);
            muteAllBtn.states = [
                ["All", Color.white, Color.grey],
                ["All", Color.white, Color.red]
            ];

            // ------------------------------------------------------------------
            sndFileView = SoundFileView.new(window, 200@150);
            sndFileView.mouseUpAction = {
                var selection = sndFileView.selections[sndFileView.currentSelection];
                // ("mouseUp, current selection is now:"
                // + selection).postln;
                samples.at(currentSample).selections[0] = selection;
            };

            // ------------------------------------------------------------------
            sndEnvView = EnvelopeView(window, 200@150)
                .drawLines_(true)
                .selectionColor_(Color.red)
                .drawRects_(true)
                .resize_(5)
                .step_(0.05)
                .action_({arg b; /*[b.index, b.value].postln*/})
                .thumbSize_(5)
                .value_([[0.0, 0.0, 0.5, 1.0],[0.0,1.0,1.0,0.0]]);

            window.front;
            // ------------------------------------------------------------------
            CmdPeriod.add({this.reset});
            // this.player.start;
            this.update;
        });

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
