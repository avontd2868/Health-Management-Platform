<%@ page import="org.osehra.cpe.datetime.format.PointInTimeFormat" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>${item.summary}</title>
    <g:render template="/layouts/detail"/>
</head>

<body>
<table class="hmp-labeled-values">

    <tr>
        <td>Type Code</td>
        <td>${item.typeCode}</td>
    </tr>
    <tr>
        <td>Type Name</td>
        <td>${item.typeName}</td>
    </tr>
    <tr>
        <td>Result</td>
        <td>${item.result}</td>
    </tr>
    <tr>
        <td>Unit</td>
        <td>${item.units}</td>
    </tr>
    <tr>
        <td>Observed</td>
        <td>${item.observed}</td>
    </tr>
    <tr>
        <td>Resulted</td>
        <td>${item.resulted}</td>
    </tr>
    <tr>
        <td>Result Status</td>
        <td>${item.resultStatus}</td>
    </tr>
    <tr>
        <td>Method Code</td>
        <td>${item.methodCode}</td>
    </tr>
    <tr>
        <td>Method Name</td>
        <td>${item.methodName}</td>
    </tr>
    <tr>
        <td>Body Site Code</td>
        <td>${item.bodySiteCode}</td>
    </tr>
    <tr>
        <td>Body Site Name</td>
        <td>${item.bodySiteName}</td>
    </tr>
    <tr>
        <td>location</td>
        <td>${item.locationName}</td>
    </tr>
    <tr>
        <td>comment</td>
        <td>${item.comment}</td>
    </tr>
    <tr>
        <td>VA Status</td>
        <td>${item.vaStatus}</td>
    </tr>
    <tr>
        <td>Qualifier Text</td>
        <td>${item.qualifierText}</td>
    </tr>
</table>
</body>
</html>
