<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="com.buildfifteen.teamcity.UEBuildGraph.BuildGraphParametersProvider"/>

<div class="parameter">
    Script Name: <strong><props:displayValue name="${params.keyScriptName}" emptyValue=""/></strong>
</div>
<div class="parameter">
    Node Name: <strong><props:displayValue name="${params.keyNodeName}" emptyValue=""/></strong>
</div>
<div class="parameter">
    Options:
    <c:set var="options" value="${fn:split(propertiesBean.properties[params.keyGraphOptions],'§')}"/>
    <c:set var="num_options" value="${(fn:length(options) / 2)-1}"/>
    <c:choose>
        <c:when test="${fn:length(options)>1}">
            <table class="settings">
                <thead>
                    <tr>
                        <th class="name" style="width: 30%;">Name</th>
                        <th class="name" style="width: 70%;">Value</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${options}" var="the_name" step="2" varStatus="theCount">
                        <c:set var="the_value" value="${options[theCount.index + 1]}"/>
                        <tr>
                            <td><c:out value="${the_name}"/></td>
                            <td><c:out value="${the_value}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <strong>None defined.</strong>
        </c:otherwise>
    </c:choose>
</div>
