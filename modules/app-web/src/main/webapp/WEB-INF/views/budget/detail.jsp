<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>예산 상세</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="plan" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/budget/budgets?month=${budget.targetMonth}" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">뒤로</a>
            <h1 class="text-base font-semibold">예산 상세</h1>
            <a href="${ctx}/budget/budgets/${budget.budgetId}/edit" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">수정</a>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <c:if test="${not empty deactivated}">
            <p class="mb-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">예산이 해제되었습니다.</p>
        </c:if>
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <section class="pv-card p-4">
            <div class="flex items-center justify-between">
                <div>
                    <div class="text-sm font-semibold">${budget.categoryName}</div>
                    <div class="mt-1 text-xs text-slate-500">${budget.targetMonth}</div>
                </div>
                <div class="text-right">
                    <div class="text-xs text-slate-500">소진율</div>
                    <div class="text-sm font-semibold ${budget.usageRate >= 100 ? 'text-red-600' : 'text-slate-900'}">${budget.usageRate}%</div>
                </div>
            </div>

            <div class="mt-3 grid grid-cols-3 gap-2 text-sm">
                <div class="rounded-xl border border-slate-200 p-3">
                    <div class="text-xs text-slate-500">목표</div>
                    <div class="mt-1 font-semibold"><fmt:formatNumber value="${budget.amountLimit}" pattern="#,##0"/></div>
                </div>
                <div class="rounded-xl border border-slate-200 p-3">
                    <div class="text-xs text-slate-500">소진</div>
                    <div class="mt-1 font-semibold"><fmt:formatNumber value="${budget.spentAmount}" pattern="#,##0"/></div>
                </div>
                <div class="rounded-xl border border-slate-200 p-3">
                    <div class="text-xs text-slate-500">잔여</div>
                    <div class="mt-1 font-semibold ${budget.remainingAmount < 0 ? 'text-red-600' : ''}">
                        <fmt:formatNumber value="${budget.remainingAmount}" pattern="#,##0"/>
                    </div>
                </div>
            </div>

            <div class="mt-3 h-2 overflow-hidden rounded-full bg-slate-200">
                <div class="h-full ${budget.usageRate >= 100 ? 'bg-red-500' : 'bg-slate-900'}" style="width: ${budget.usageRate > 100 ? 100 : budget.usageRate}%"></div>
            </div>

            <c:if test="${not empty budget.memo}">
                <div class="mt-4 rounded-xl border border-slate-200 p-3 text-sm text-slate-600">${budget.memo}</div>
            </c:if>
        </section>

        <form method="post" action="${ctx}/budget/budgets/${budget.budgetId}" class="mt-4" data-ajax="true" data-method="DELETE">
            <button type="submit" class="w-full rounded-xl border border-red-200 px-3 py-2 text-sm text-red-700 transition hover:bg-red-50">예산 해제</button>
        </form>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
