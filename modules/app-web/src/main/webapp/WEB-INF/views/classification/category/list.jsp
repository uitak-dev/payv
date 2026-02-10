<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>카테고리 관리</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="settings" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <h1 class="text-base font-semibold">카테고리 관리</h1>
            <a href="${ctx}/classification/tags" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">태그</a>
        </div>
    </header>

    <main class="px-4 pt-4 pv-safe-bottom">
        <c:if test="${not empty createdRoot}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">1단계 카테고리가 생성되었습니다.</p>
        </c:if>
        <c:if test="${not empty createdChild}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">2단계 카테고리가 생성되었습니다.</p>
        </c:if>
        <c:if test="${not empty renamed}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">카테고리 이름이 변경되었습니다.</p>
        </c:if>
        <c:if test="${not empty deactivated}">
            <p class="mb-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">카테고리가 비활성화되었습니다.</p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <div class="space-y-4">
            <section class="pv-card p-4">
                <div class="text-sm font-semibold">1단계 카테고리 추가</div>
                <form class="mt-3 flex gap-2" method="post" action="${ctx}/classification/categories/roots" data-ajax="true">
                    <input name="name" required class="flex-1 rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="예: 식비"/>
                    <button type="submit" class="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white">추가</button>
                </form>
            </section>

            <section class="space-y-3">
                <c:forEach var="root" items="${categories}">
                    <article class="pv-card p-4">
                        <div class="flex items-center justify-between gap-2">
                            <div class="text-sm font-semibold">${root.name}</div>
                            <form method="post" action="${ctx}/classification/categories/roots/${root.categoryId}" data-ajax="true" data-method="DELETE">
                                <button type="submit" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">비활성</button>
                            </form>
                        </div>

                        <form class="mt-3 flex gap-2" method="post" action="${ctx}/classification/categories/roots/${root.categoryId}" data-ajax="true" data-method="PUT">
                            <input name="newName" value="${root.name}" required class="flex-1 rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                            <button type="submit" class="rounded-xl border border-slate-200 px-3 py-2 text-sm">1단계 이름변경</button>
                        </form>

                        <div class="mt-3 space-y-2">
                            <c:forEach var="child" items="${root.children}">
                                <div class="rounded-lg border border-slate-200 p-2">
                                    <form class="flex gap-2" method="post" action="${ctx}/classification/categories/roots/${root.categoryId}/children/${child.categoryId}" data-ajax="true" data-method="PUT">
                                        <input name="newName" value="${child.name}" required class="flex-1 rounded-lg border border-slate-200 px-3 py-2 text-sm"/>
                                        <button type="submit" class="rounded-lg border border-slate-200 px-3 py-2 text-sm">2단계 이름변경</button>
                                    </form>
                                    <form class="mt-2" method="post" action="${ctx}/classification/categories/roots/${root.categoryId}/children/${child.categoryId}" data-ajax="true" data-method="DELETE">
                                        <button type="submit" class="text-xs text-slate-500">비활성</button>
                                    </form>
                                </div>
                            </c:forEach>
                        </div>

                        <form class="mt-3 flex gap-2" method="post" action="${ctx}/classification/categories/roots/${root.categoryId}/children" data-ajax="true">
                            <input name="name" required class="flex-1 rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="2단계 추가"/>
                            <button type="submit" class="rounded-xl border border-slate-200 px-3 py-2 text-sm">추가</button>
                        </form>
                    </article>
                </c:forEach>
                <c:if test="${empty categories}">
                    <section class="pv-card p-4 text-sm text-slate-500">등록된 카테고리가 없습니다.</section>
                </c:if>
            </section>
        </div>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
