<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>2단계 카테고리 수정</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="settings" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/classification/categories/roots/${root.categoryId}" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">뒤로</a>
            <h1 class="text-base font-semibold">2단계 카테고리 수정</h1>
            <span class="w-12"></span>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <form class="space-y-4" method="post" action="${ctx}/classification/categories/roots/${root.categoryId}/children/${child.categoryId}" data-ajax="true" data-method="PUT">
            <section class="pv-card p-4">
                <div class="text-xs text-slate-500">상위 카테고리</div>
                <div class="mt-1 text-sm font-medium">${root.name}</div>
                <label class="mt-4 block">
                    <div class="text-sm font-medium">카테고리 이름</div>
                    <input name="newName" value="${child.name}" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                </label>
            </section>
            <button type="submit" class="w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white">수정</button>
        </form>

        <form class="mt-4" method="post" action="${ctx}/classification/categories/roots/${root.categoryId}/children/${child.categoryId}" data-ajax="true" data-method="DELETE">
            <button type="submit" class="w-full rounded-xl border border-red-200 px-3 py-2 text-sm text-red-700 transition hover:bg-red-50">2단계 카테고리 삭제</button>
        </form>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
