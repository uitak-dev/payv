<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>설정</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="settings" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/home" class="inline-flex items-center rounded-lg border border-slate-200 px-3 py-1.5 text-sm">홈</a>
            <h1 class="text-base font-semibold">설정</h1>
            <div class="inline-flex items-center rounded-lg border border-slate-200 px-2.5 py-1.5 text-xs text-slate-600">
                <c:choose>
                    <c:when test="${not empty ownerUserId}">${ownerUserId}</c:when>
                    <c:otherwise>-</c:otherwise>
                </c:choose>
            </div>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <div class="space-y-4">
            <section class="pv-card p-4">
                <div class="text-sm font-semibold">설정</div>
                <div class="mt-3 space-y-2">
                    <a href="${ctx}/classification/categories" class="flex items-center justify-between rounded-xl border border-slate-200 px-4 py-3">
                        <span class="text-sm font-medium">카테고리 관리</span><img src="${ctx}/assets/icons/chevron-right.svg" alt="" class="h-4 w-4 opacity-70" aria-hidden="true"/>
                    </a>
                    <a href="${ctx}/classification/tags" class="flex items-center justify-between rounded-xl border border-slate-200 px-4 py-3">
                        <span class="text-sm font-medium">태그 관리</span><img src="${ctx}/assets/icons/chevron-right.svg" alt="" class="h-4 w-4 opacity-70" aria-hidden="true"/>
                    </a>
                    <a href="${ctx}/asset/assets" class="flex items-center justify-between rounded-xl border border-slate-200 px-4 py-3">
                        <span class="text-sm font-medium">자산 관리</span><img src="${ctx}/assets/icons/chevron-right.svg" alt="" class="h-4 w-4 opacity-70" aria-hidden="true"/>
                    </a>
                </div>
            </section>

            <section class="pv-card p-4">
                <form method="post" action="${ctx}/logout" data-ajax="true">
                    <button type="submit" class="inline-flex w-full items-center justify-center rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold">
                        로그아웃
                    </button>
                </form>
            </section>

            <c:if test="${not empty email}">
                <section class="pv-card p-4">
                    <div class="text-xs text-slate-500">사용자 프로필</div>
                    <div class="mt-1 text-sm font-medium">${email}</div>
                </section>
            </c:if>
        </div>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
