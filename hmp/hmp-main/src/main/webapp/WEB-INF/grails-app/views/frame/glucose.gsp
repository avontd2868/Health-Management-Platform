<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.osehra.cpe.vpr.*"%>
<%@ page import="org.springframework.data.domain.Page"%>

<html>
<head>
    <title>Glucose</title>
    <g:render template="/layouts/detail"/>
</head>

<body>

<div id="chartTargetID"></div>

<div>
	Add a patient observed value: <input type="text" value="9/10/2012">
	value: <input type="text" value="100"> <input type="submit">
</div>

<g:javascript>
	Ext.onReady(function() {
	
		var x = Ext.create('CPE.view.ChartPanel', {
			height: 200,
			width: 400,
			renderTo: 'chartTargetID'
		});
		
		x.updateChart([{
	            name: 'Lab Results',
	            type: 'line',
	            data: [
	                [Date.UTC(2011,  9, 27), 0   ],
	                [Date.UTC(2011, 10, 10), 0.6 ]
	            ]
	        }]);
	});
</g:javascript>

</body>
</html>
