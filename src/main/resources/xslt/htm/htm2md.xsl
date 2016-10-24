<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="text"/>

	<xsl:template match="@*|node()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="text()">
		<xsl:if test="normalize-space(.) != ''">
			<xsl:value-of select="."/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="h1">
		<xsl:text># </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="h2">
		<xsl:text>## </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="h3">
		<xsl:text>### </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="h4">
		<xsl:text>#### </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="h5">
		<xsl:text>##### </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="h6">
		<xsl:text>###### </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="ul">
		<xsl:apply-templates/>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>

	<xsl:template match="ul/li">
		<xsl:for-each select="../ancestor::*[local-name(.)='ol' or local-name(.)='ul']">
			<xsl:text>  </xsl:text>
		</xsl:for-each>
		<xsl:text>* </xsl:text>
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="p">
		<xsl:apply-templates/>
		<xsl:if test="not(ancestor::td or ancestor::th)">
			<xsl:text>&#xa;</xsl:text>
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="a">
		<xsl:text>[</xsl:text>
		<xsl:choose>
			<xsl:when test=".">
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@href"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>]</xsl:text>
		<xsl:text>(</xsl:text>
		<xsl:value-of select="@href"/>
		<xsl:text>)</xsl:text>
	</xsl:template>


	<xsl:template match="strong | b">
		<xsl:text>**</xsl:text>
		<xsl:apply-templates/>
		<xsl:text>**</xsl:text>
	</xsl:template>

	<xsl:template match="em">
		<xsl:text>*</xsl:text>
		<xsl:apply-templates/>
		<xsl:text>*</xsl:text>
	</xsl:template>

	<xsl:template match="code">
		<xsl:text>`</xsl:text>
		<xsl:apply-templates/>
		<xsl:text>`</xsl:text>
	</xsl:template>

	<xsl:template match="br">
		<xsl:text>  &#xa;</xsl:text>
	</xsl:template>

	<!--
	code
	structured ...
	-->


	<xsl:template match="table">
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="tr[th]">
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
		<xsl:apply-templates mode="header-dashes"/>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="tr[td]">
		<xsl:apply-templates/>
		<xsl:text>&#xa;</xsl:text>
	</xsl:template>

	<xsl:template match="th">
		<xsl:text>|</xsl:text>
		<xsl:apply-templates/>
		<xsl:if test="not(following-sibling::th)">
			<xsl:text>|</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="th" mode="header-dashes">
		<xsl:text>|</xsl:text>
		<xsl:variable name="text">
			<xsl:apply-templates/>
		</xsl:variable>
		<xsl:for-each select="1 to string-length($text)">
			<xsl:text>-</xsl:text>
		</xsl:for-each>
		<xsl:if test="not(following-sibling::th)">
			<xsl:text>|</xsl:text>
		</xsl:if>
	</xsl:template>


	<xsl:template match="td">
		<xsl:text>|</xsl:text>
		<xsl:apply-templates/>
		<xsl:if test="not(following-sibling::td)">
			<xsl:text>|</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="img">
		<xsl:text>![]</xsl:text>
		<xsl:text>(</xsl:text>
		<xsl:apply-templates mode="link-target"/>
		<xsl:text>)</xsl:text>
	</xsl:template>

	<xsl:template match="attachment" mode="link-target">
		<xsl:value-of select="@filename"/>
	</xsl:template>

	<!--
	attachments emoticons
	-->

</xsl:stylesheet>