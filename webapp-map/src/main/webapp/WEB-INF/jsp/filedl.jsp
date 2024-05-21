<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
    <title>${viewName}</title>
    <meta name="description" content="Tampereen avoimien aineistojen latauspalvelu" />
    <link rel="shortcut icon" href="${clientDomain}/Oskari${path}/logo.png" type="image/png" />
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <!-- IE 9 polyfill for openlayers 3 - https://github.com/openlayers/ol3/issues/4865 -->
    <!--[if lte IE 9]> <script src="https://cdn.polyfill.io/v2/polyfill.min.js?features=fetch,requestAnimationFrame,Element.prototype.classList"></script> <![endif]-->

    <!-- ############# css ################# -->
    <link
            rel="stylesheet"
            type="text/css"
            href="${clientDomain}/Oskari${path}/icons.css"/>

    <link
            rel="stylesheet"
            type="text/css"
            href="${clientDomain}/Oskari${path}/oskari.min.css"/>

    <link href="https://fonts.googleapis.com/css?family=Noto+Sans" rel="stylesheet">
    <!-- ############# /css ################# -->
</head>
<body id="oskari">


<!-- ############# Javascript ################# -->

<!--  OSKARI -->

<script type="text/javascript">
    var ajaxUrl = '${ajaxUrl}';
    var controlParams = ${controlParams};
</script>
<%-- Pre-compiled application JS, empty unless created by build job --%>
<script type="text/javascript"
        src="${clientDomain}/Oskari${path}/oskari.min.js">
</script>

<%-- language files --%>
<script type="text/javascript"
        src="${clientDomain}/Oskari${path}/oskari_lang_${language}.js">
</script>

<script type="text/javascript"
        src="${clientDomain}/Oskari${path}/index.js">
</script>


<!-- ############# /Javascript ################# -->
</body>
</html>
