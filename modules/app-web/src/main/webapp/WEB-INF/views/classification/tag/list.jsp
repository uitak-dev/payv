<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>태그 관리</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="settings" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <h1 class="text-base font-semibold">태그 관리</h1>
            <a href="${ctx}/classification/categories" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">카테고리</a>
        </div>
    </header>

    <main class="px-4 pt-4 pv-safe-bottom">
        <c:if test="${not empty created}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">태그가 생성되었습니다.</p>
        </c:if>
        <c:if test="${not empty renamed}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">태그 이름이 변경되었습니다.</p>
        </c:if>
        <c:if test="${not empty deactivated}">
            <p class="mb-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">태그가 비활성화되었습니다.</p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <div class="space-y-4">
            <section class="pv-card p-4">
                <div class="text-sm font-semibold">태그 추가</div>
                <form class="mt-3 flex gap-2" method="post" action="${ctx}/classification/tags" data-ajax="true">
                    <input name="name" required class="flex-1 rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="예: 회사"/>
                    <button type="submit" class="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white">추가</button>
                </form>
            </section>

            <section class="pv-card p-4">
                <div class="text-sm font-semibold">태그 목록</div>
                <div class="mt-4 space-y-3">
                    <c:forEach var="tag" items="${tags}">
                        <div class="rounded-xl border border-slate-200 p-3">
                            <form class="flex gap-2" method="post" action="${ctx}/classification/tags/${tag.tagId}" data-ajax="true" data-method="PUT">
                                <input name="newName" value="${tag.name}" required class="flex-1 rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                                <button type="submit" class="rounded-xl border border-slate-200 px-3 py-2 text-sm">이름변경</button>
                            </form>
                            <form class="mt-2" method="post" action="${ctx}/classification/tags/${tag.tagId}" data-ajax="true" data-method="DELETE">
                                <button type="submit" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">비활성</button>
                            </form>
                        </div>
                    </c:forEach>
                    <c:if test="${empty tags}">
                        <p class="py-1 text-sm text-slate-500">등록된 태그가 없습니다.</p>
                    </c:if>
                </div>
            </section>
        </div>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
