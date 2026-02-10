<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>예산 설정</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="plan" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <h1 class="text-base font-semibold">예산 설정</h1>
            <a href="${ctx}/budget/budgets/new?month=${selectedMonth}" class="rounded-xl bg-slate-900 px-3 py-1.5 text-sm font-semibold text-white">예산 추가</a>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <section class="pv-card p-4">
            <form method="get" action="${ctx}/budget/budgets" class="flex items-center gap-2">
                <input type="month" name="month" value="${selectedMonth}" class="rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                <button type="submit" class="rounded-xl border border-slate-200 px-3 py-2 text-sm">조회</button>
            </form>
        </section>

        <c:if test="${not empty notice.created}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">예산이 생성되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.updated}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">예산이 수정되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.deactivated}">
            <p class="mb-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">예산이 해제되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <section class="mt-4 space-y-3">
            <c:forEach var="item" items="${budgets}">
                <article class="pv-card p-4">
                    <a href="${ctx}/budget/budgets/${item.budgetId}" class="block">
                        <div class="flex items-center justify-between">
                            <div>
                                <div class="text-sm font-semibold">${item.categoryName}</div>
                                <div class="mt-1 text-xs text-slate-500">${item.targetMonth}</div>
                            </div>
                            <div class="text-right">
                                <div class="text-xs text-slate-500">소진율</div>
                                <div class="text-sm font-semibold ${item.usageRate >= 100 ? 'text-red-600' : 'text-slate-900'}">${item.usageRate}%</div>
                            </div>
                        </div>

                        <div class="mt-3 grid grid-cols-3 gap-2 text-sm">
                            <div class="rounded-xl border border-slate-200 p-3">
                                <div class="text-xs text-slate-500">목표</div>
                                <div class="mt-1 font-semibold"><fmt:formatNumber value="${item.amountLimit}" pattern="#,##0"/></div>
                            </div>
                            <div class="rounded-xl border border-slate-200 p-3">
                                <div class="text-xs text-slate-500">소진</div>
                                <div class="mt-1 font-semibold"><fmt:formatNumber value="${item.spentAmount}" pattern="#,##0"/></div>
                            </div>
                            <div class="rounded-xl border border-slate-200 p-3">
                                <div class="text-xs text-slate-500">잔여</div>
                                <div class="mt-1 font-semibold ${item.remainingAmount < 0 ? 'text-red-600' : ''}">
                                    <fmt:formatNumber value="${item.remainingAmount}" pattern="#,##0"/>
                                </div>
                            </div>
                        </div>

                        <div class="mt-3 h-2 overflow-hidden rounded-full bg-slate-200">
                            <div class="h-full ${item.usageRate >= 100 ? 'bg-red-500' : 'bg-slate-900'}" style="width: ${item.usageRate > 100 ? 100 : item.usageRate}%"></div>
                        </div>
                    </a>
                </article>
            </c:forEach>
            <c:if test="${empty budgets}">
                <section class="pv-card p-4 text-sm text-slate-500">선택한 월에 조회된 예산이 없습니다.</section>
            </c:if>
        </section>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
