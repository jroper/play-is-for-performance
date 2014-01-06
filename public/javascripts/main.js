require(["PerformanceTest", "Navigate", "webjars!jquery.js", "webjars!prettify.js"],
    function(PerformanceTest, Navigate) {
        $(function() {
            $(".performanceTest").each(function() {
                new PerformanceTest($(this));
            });
            prettyPrint();
            new Navigate();
        });
    }
);


