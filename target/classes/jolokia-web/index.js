
function JmxChartsFactory(keepHistorySec, pollInterval, columnsCount) {
    var jolokia = new Jolokia("/jmx");
    var series = [];
    var monitoredMbeans = [];
    var chartsCount = 0;

    columnsCount = columnsCount || 3;
    pollInterval = pollInterval || 1000;
    var keepPoints = (keepHistorySec || 60) / (pollInterval / 1000);

    setupPortletsContainer(columnsCount);

    setInterval(function() {
        pollAndUpdateCharts();
    }, pollInterval);

    this.create = function(mbeans) {
        mbeans = $.makeArray(mbeans);
        series = series.concat(new JMXChart(mbeans));
        monitoredMbeans = monitoredMbeans.concat(mbeans);
    };

    function pollAndUpdateCharts() {
        var requests = prepareBatchRequest();
        var responses = jolokia.request(requests);
        updateCharts(responses);
    }

    function createNewPortlet(name) {
        return $('#portlet-template')
                .clone(true)
                .appendTo($('.column')[chartsCount++ % columnsCount])
                .removeAttr('id')
                .find('.title').text((name.length > 50? '...' : '') + name.substring(name.length - 50, name.length)).end()
                .find('.portlet-content')[0];
    }

    function setupPortletsContainer() {
        var column = $('.column');
        for(var i = 1; i < columnsCount; ++i){
            column.clone().appendTo(column.parent());
        }
        $(".column").sortable({
            connectWith: ".column"
        });

        $(".portlet-header .ui-icon").click(function() {
            $(this).toggleClass("ui-icon-minusthick").toggleClass("ui-icon-plusthick");
            $(this).parents(".portlet:first").find(".portlet-content").toggle();
        });
        $(".column").disableSelection();
    }

    function prepareBatchRequest() {
        return $.map(monitoredMbeans, function(mbean) {
            return {
                type: "read",
                mbean: mbean.name,
                attribute: mbean.attribute,
                path: mbean.path
            };
        });
    }

    function updateCharts(responses) {
        var curChart = 0;
        $.each(responses, function() {
            var point = [
                this.timestamp * 1000,
                parseFloat(this.value)
            ];
            var chart = series[curChart++];
            chart.addPoint(point);
        });
    }
    
    
    function JMXChart(mbeans) {
   	    var options = {
			series: { shadowSize: 0 }, // drawing is faster without shadows
			yaxis: { },
			xaxis: { mode: "time" }
		};
		this.plot = $.plot(createNewPortlet(mbeans[0].name), [ [0,0] ], options);
	 
		this.data = [];
		
		this.addPoint = function(point) {
			while (this.data.length >= keepPoints) {
				this.data = this.data.slice(1);
			}
			this.data.push(point);
			this.plot.setData([ this.data ]);
			this.plot.setupGrid();
			this.plot.draw();
    	}
    }
    

    
}