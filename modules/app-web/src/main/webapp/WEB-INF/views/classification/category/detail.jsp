<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>카테고리 상세</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="settings" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/classification/categories" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">뒤로</a>
            <h1 class="text-base font-semibold">카테고리 상세</h1>
            <a href="${ctx}/classification/categories/roots/${root.categoryId}/edit" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">수정</a>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <c:if test="${not empty createdChild}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">2단계 카테고리가 생성되었습니다.</p>
        </c:if>
        <c:if test="${not empty renamed}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">카테고리 이름이 변경되었습니다.</p>
        </c:if>
        <c:if test="${not empty deactivated}">
            <p class="mb-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">카테고리가 삭제되었습니다.</p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <section class="pv-card p-4">
            <div class="flex items-center justify-between">
                <div>
                    <div class="text-sm font-semibold">${root.name}</div>
                    <div class="mt-1 text-xs text-slate-500">하위 ${root.children.size()}개</div>
                </div>
                <a href="${ctx}/classification/categories/roots/${root.categoryId}/children/new" class="rounded-xl bg-slate-900 px-3 py-1.5 text-sm font-semibold text-white">2단계 추가</a>
            </div>
        </section>

        <section class="mt-4 space-y-3">
            <c:forEach var="child" items="${root.children}">
                <article class="pv-card p-4">
                    <div class="flex items-center justify-between gap-2">
                        <div class="text-sm font-medium">${child.name}</div>
                        <a href="${ctx}/classification/categories/roots/${root.categoryId}/children/${child.categoryId}/edit" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">수정</a>
                    </div>
                </article>
            </c:forEach>
            <c:if test="${empty root.children}">
                <section class="pv-card p-4 text-sm text-slate-500">하위 카테고리가 없습니다.</section>
            </c:if>
        </section>

        <form class="mt-4" method="post" action="${ctx}/classification/categories/roots/${root.categoryId}" data-ajax="true" data-method="DELETE">
            <button type="submit" class="w-full rounded-xl border border-red-200 px-3 py-2 text-sm text-red-700">1단계 카테고리 삭제</button>
        </form>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
