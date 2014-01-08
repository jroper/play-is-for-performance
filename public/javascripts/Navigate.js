define(["webjars!jquery.js"], function() {
   function slideLink(rel) {
       var elem = $("link[rel='" + rel + "']").get(0);
       if (elem) {
           return $(elem).attr("href");
       }
   }

    function Navigate() {
        this.nextSlide = slideLink("nextSlide");
        this.prevSlide = slideLink("prevSlide");
        this.remainingPoints = [];
        this.viewedPoints = [];

        var points = $(".point, .action");
        if (location.search.indexOf("prev") >= 0) {
            for (var i = 0; i < points.length; i++) {
                var point = $(points[i]);
                point.show();
                this.viewedPoints[i] = point;
            }
        } else {
            for (var i = 0; i < points.length; i++) {
                var point = $(points[i]);
                if (i == 0 && !point.hasClass("action")) {
                    point.show();
                } else {
                    this.remainingPoints.push(point);
                }
            }
            this.remainingPoints.reverse();
        }

        var self = this;

        $(window).keydown(function(e) {
            switch(e.keyCode) {
                case 34: // page down
                case 39: // right
                case 40: // down
                case 32: // space
                    if (self.remainingPoints.length == 0) {
                        if (self.nextSlide) {
                            window.location = self.nextSlide;
                        }
                    } else {
                        var point = self.remainingPoints.pop();
                        if (point.hasClass("point")) {
                            point.show();
                        }
                        if (point.hasClass("action")) {
                            point.click();
                        }
                        self.viewedPoints.push(point);
                    }
                    break;
                case 33: // page up
                case 37: // left
                case 38: // up
                    if (self.viewedPoints.length == 0) {
                        if (self.prevSlide) {
                            window.location = self.prevSlide + "?prev=true";
                        }
                    } else {
                        var point = self.viewedPoints.pop();
                        if (point.hasClass("point")) {
                            point.hide();
                        }
                        self.remainingPoints.push(point);
                    }
                    break;
                case 88: // x
                    $(".blackOut").toggle();
                    break;
            }
        });
    }

    return Navigate;
});