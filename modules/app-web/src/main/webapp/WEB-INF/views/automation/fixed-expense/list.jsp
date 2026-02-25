<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>고정비 관리</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="settings" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <h1 class="text-base font-semibold">고정비 관리</h1>
            <a href="${ctx}/automation/fixed-expenses/new" class="rounded-xl bg-slate-900 px-3 py-1.5 text-sm font-semibold text-white">고정비 추가</a>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <c:if test="${not empty notice.created}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">고정비가 추가되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.updated}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">고정비가 수정되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.deactivated}">
            <p class="mb-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">고정비가 삭제되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <section class="pv-card p-4">
            <div class="text-sm font-semibold">고정비 목록</div>
            <div class="mt-4 space-y-3">
                <c:forEach var="item" items="${fixedExpenses}">
                    <article class="rounded-xl border border-slate-200 p-3">
                        <div class="flex items-start justify-between gap-3">
                            <div>
                                <div class="text-sm font-semibold">${item.name}</div>
                                <div class="mt-1 text-xs text-slate-500">${item.scheduleLabel}</div>
                            </div>
                            <div class="text-right">
                                <div class="text-xs text-slate-500">금액</div>
                                <div class="text-sm font-semibold"><fmt:formatNumber value="${item.amount}" pattern="#,##0"/></div>
                            </div>
                        </div>

                        <div class="mt-3 grid grid-cols-1 gap-1 text-xs text-slate-600">
                            <div>자산: ${empty item.assetName ? '-' : item.assetName}</div>
                            <div>카테고리: ${empty item.categoryNameLevel1 ? '-' : item.categoryNameLevel1}
                                <c:if test="${not empty item.categoryNameLevel2}"> / ${item.categoryNameLevel2}</c:if>
                            </div>
                            <c:if test="${not empty item.memo}">
                                <div>메모: ${item.memo}</div>
                            </c:if>
                        </div>

                        <div class="mt-3 flex items-center justify-end gap-2">
                            <a href="${ctx}/automation/fixed-expenses/${item.definitionId}/edit" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">수정</a>
                            <form method="post" action="${ctx}/automation/fixed-expenses/${item.definitionId}" data-ajax="true" data-method="DELETE">
                                <button type="submit" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">삭제</button>
                            </form>
                        </div>
                    </article>
                </c:forEach>
                <c:if test="${empty fixedExpenses}">
                    <p class="text-sm text-slate-500">등록된 고정비가 없습니다.</p>
                </c:if>
            </div>
        </section>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
