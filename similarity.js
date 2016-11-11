/// reads in a CSV made from the data above
/// Makes a scatterplot with labels

$(document).ready(function () 
    {
        $.get("/similarity_control/", function(data) 
        {
            $("#fileNames").append(data);
        });
    })

var labels = ['Similarity to Control'];
	
var columns = [0,1,2,3]; //must be in order
var colNames = ['number', 'control', 'word', 'label']; // indexed correctly
var selectedX = colNames[0];
var selectedY = colNames[1];

var margin = {top: 20, right: 20, bottom: 30, left: 40},
    width = 1160 - margin.left - margin.right,
    height = 500 - margin.top - margin.bottom;

var x = d3.scale.linear().range([0, width]);
var y = d3.scale.linear().range([height, 0]);
var color = d3.scale.linear().range(["yellow","red"]);

update();

function update(){
	
//var fileName = "similarity_control/ArmySgt1961.csv"
//var fileName = "similarity_control/arthinice.csv"
//var fileName = "similarity_control/asian_kreationz.csv"
var fileName = "similarity_control/aticloose.csv"
	
console.log("drawing " + selectedX + " vs " + selectedY);

var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom");

var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left");

var svg = d3.select("body").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var data = d3.csv(fileName, function(error, data) {
	if (error) throw error;

  data.forEach(function(d) {
    d[selectedX] = +d[selectedX]; 
    d[selectedY] = +d[selectedY]; 
  });
  
  console.log(data);

  x.domain(d3.extent(data, function(d) { return d[selectedX]; })).nice(); 
  y.domain([0,1]);
  color.domain([0,1]);


  // x-axis
  svg.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)
    .append("text")
      .attr("class", "label")
      .attr("x", width)
      .attr("y", -6)
      .style("text-anchor", "end")
      .text("Word Number");
  
  // y-axis
  svg.append("g")
      .attr("class", "y axis")
      .call(yAxis)
    .append("text")
      .attr("class", "label")
      .attr("transform", "rotate(-90)")
      .attr("y", 6)
      .attr("dy", ".71em")
      .style("text-anchor", "end")
      .text("Score");
  
  // dots
  svg.selectAll(".dot")
      .data(data)
    .enter().append("circle")
      .attr("class", "dot")
      .attr("r", 3.5)
      .attr("cx", function(d) { return x(d[selectedX]); }) 
      .attr("cy", function(d) { return y(d[selectedY]); }) 
      .attr("fill", function(d) {
			return color(d[selectedY]);
	   	})

      
 	// Define the line
	var valueline = d3.svg.line()
    	.x(function(d) { return x(d[selectedX]); })
    	.y(function(d) { return y(d[selectedY]); })
    	.interpolate("linear");
    
    var xSeries = data.map(function(d) { return parseFloat(d[selectedX]); });
	var ySeries = data.map(function(d) { return parseFloat(d[selectedY]); });
    
    if(xSeries.length < 1000){
	// Add the connecting lines
	svg.append("path")
		.attr("class", "line")
		.attr("d", valueline(data));
	}	
		
	// Add a linear regression	
	var leastSquaresCoeff = leastSquares(xSeries, ySeries);
		
	// apply the reults of the least squares regression
	var x1 = xSeries[0];
	var y1 = leastSquaresCoeff[0] + leastSquaresCoeff[1];
	var x2 = xSeries[xSeries.length - 1];
	var y2 = leastSquaresCoeff[0] * xSeries.length + leastSquaresCoeff[1];
	var trendData = [[x1,y1,x2,y2]];
	
	console.log("slope: " + leastSquaresCoeff[0]);
	console.log("intercept: " + leastSquaresCoeff[1]);
	console.log("r^2: " + leastSquaresCoeff[2]);
	
	var trendline = svg.selectAll(".trendline")
		.data(trendData);
		
	trendline.enter()
		.append("line")
		.attr("class", "trendline")
		.attr("x1", function(d) { return x(d[0]); })
		.attr("y1", function(d) { return y(d[1]); })
		.attr("x2", function(d) { return x(d[2]); })
		.attr("y2", function(d) { return y(d[3]); })
		.attr("stroke", "blue")
		.attr("stroke-width", 1);
	
});

// returns slope, intercept and r-square of the line
	function leastSquares(xSeries, ySeries) {
		var reduceSumFunc = function(prev, cur) { return prev + cur; };
		
		var xBar = xSeries.reduce(reduceSumFunc) * 1.0 / xSeries.length;
		var yBar = ySeries.reduce(reduceSumFunc) * 1.0 / ySeries.length;

		var ssXX = xSeries.map(function(d) { return Math.pow(d - xBar, 2); })
			.reduce(reduceSumFunc);
		
		var ssYY = ySeries.map(function(d) { return Math.pow(d - yBar, 2); })
			.reduce(reduceSumFunc);
			
		var ssXY = xSeries.map(function(d, i) { return (d - xBar) * (ySeries[i] - yBar); })
			.reduce(reduceSumFunc);
			
		var slope = ssXY / ssXX;
		var intercept = yBar - (xBar * slope);
		var rSquare = Math.pow(ssXY, 2) / (ssXX * ssYY);
		
		return [slope, intercept, rSquare];
	}
}