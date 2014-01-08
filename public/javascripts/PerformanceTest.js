define(["webjars!jquery.js", "webjars!Chart.js"], function() {

    function PerformanceTest(perfTestElem) {
        this.perfTestElem = perfTestElem;
        this.button = this.perfTestElem.find(".start");
        this.statsElem = this.perfTestElem.find(".stats");
        this.testsElem = this.perfTestElem.find(".tests");
        this.resultsElem = this.perfTestElem.find(".results");

        this.running = false;

        self = this;

        this.button.click(function() {

            if (self.running) {
                return;
            }
            self.running = true;

            self.testsElem.empty();

            self.resultsElem.hide();
            self.button.attr("disabled", "disabled");
            self.statsElem.show();
            self.testsElem.show();

            var events = new EventSource(self.perfTestElem.data("url"));

            self.lastProgress = new Date().getTime();

            events.addEventListener("progress", function(e) {
                var time = new Date().getTime();
                var delay = time - self.lastProgress;
                self.lastProgress = time;
                var data = JSON.parse(e.data);
                for (var i = 0; i < data.tests.length; i++) {
                    var test = data.tests[i];
                    self.updateProgress(test, time, delay);
                }

                self.updateGauge($(".cpuUsage .gauge"), data.stats.cpuUsage);
                self.updateGauge($(".memoryUsage .gauge"), data.stats.memoryUsage);
                self.updateGauge($(".loadAverage .gauge"), data.stats.loadAverage * 20);
            });

            events.addEventListener("results", function(e) {
                var data = JSON.parse(e.data);

                self.running = false;
                self.statsElem.hide();
                self.testsElem.hide();
                self.button.removeAttr("disabled");

                self.resultsElem.empty();
                self.resultsElem.show();

                var canvas = $("<canvas>").attr({width: 400, height: 400});
                self.resultsElem.append(canvas);

                var ctx = canvas.get(0).getContext("2d");

                var labels = [];
                var points = [];
                var maxTime = 0;
                for (var i = 0; i < data.results.length; i++) {
                    var result = data.results[i];
                    labels[i] = result.name;
                    points[i] = result.time;
                    maxTime = Math.max(maxTime, result.time);
                }

                var stepWidth = Math.ceil(maxTime / 20)

                new Chart(ctx).Bar({
                    labels: labels,
                    datasets: [{
                        data: points,
                        fillColor : "rgba(151,187,205,0.5)",
                        strokeColor : "rgba(151,187,205,1)"
                    }]
                }, {
                    scaleOverride: true,
                    scaleStartValue: 0,
                    scaleSteps: 21,
                    scaleStepWidth: stepWidth
                });

                events.close();
            });
        });

    }

    PerformanceTest.prototype.updateProgress = function(test, time, delay) {
        var elem = this.testsElem.find("." + test.name);
        if (elem.length == 0) {
            var testElem = $("<li>").addClass(test.name);
            testElem.append($("<div>").addClass("testName").text(test.name));
            testElem.append($("<span>").addClass("progressBar").append(
                $("<span>").addClass("progress")
            ));

            $(".tests").append(testElem);
        }

        elem.find(".progress").css({
            width: test.progress + "%",
            transition: "width " + delay + "ms linear"
        });
    };

    PerformanceTest.prototype.updateGauge = function(gauge, value) {
        var needleDeg = value - 50;
        gauge.find(".needle").css("-webkit-transform", "rotate(" + needleDeg + "deg)");

        value = Math.abs(value);
        // 0 will be HSL: 120deg, 100%, 63% (light green)
        // 100 will be HSL: 0deg, 100%, 50% (red)
        var hsl = {
            h: (value * -1.2 + 120) / 360,
            s: 1.0,
            l: (value * -0.13 + 63) / 100
        };
        gauge.find(".bar").css("border-top-color", this.rgbToHex(this.hslToRgb(hsl)));
    };

    PerformanceTest.prototype.hslToRgb = function(hsl) {
        var r, g, b;
        var h = hsl.h, s = hsl.s, l = hsl.l;

        if(s == 0){
            r = g = b = l; // achromatic
        }else{
            function hue2rgb(p, q, t){
                if(t < 0) t += 1;
                if(t > 1) t -= 1;
                if(t < 1/6) return p + (q - p) * 6 * t;
                if(t < 1/2) return q;
                if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
                return p;
            }

            var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            var p = 2 * l - q;
            r = hue2rgb(p, q, h + 1/3);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1/3);
        }

        return {r: Math.floor(r * 255), g: Math.floor(g * 255), b: Math.floor(b * 255)};
    };

    PerformanceTest.prototype.rgbToHex = function(rgb) {
        return "#" + ((1 << 24) + (rgb.r << 16) + (rgb.g << 8) + rgb.b).toString(16).slice(1);
    };
    return PerformanceTest
});