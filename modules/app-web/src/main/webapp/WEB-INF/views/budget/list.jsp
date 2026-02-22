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
            <p class="mt-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">예산이 생성되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.updated}">
            <p class="mt-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">예산이 수정되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.deactivated}">
            <p class="mt-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">예산이 해제되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.error}">
            <p class="mt-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <c:set var="overallBudget" value="${null}"/>
        <c:set var="categoryBudgetCount" value="0"/>
        <c:set var="categoryBudgetTotal" value="0"/>
        <c:set var="categorySpentTotal" value="0"/>

        <c:forEach var="item" items="${budgets}">
            <c:choose>
                <c:when test="${empty item.categoryId}">
                    <c:set var="overallBudget" value="${item}"/>
                </c:when>
                <c:otherwise>
                    <c:set var="categoryBudgetCount" value="${categoryBudgetCount + 1}"/>
                    <c:set var="categoryBudgetTotal" value="${categoryBudgetTotal + item.amountLimit}"/>
                    <c:set var="categorySpentTotal" value="${categorySpentTotal + item.spentAmount}"/>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <c:set var="categoryRemainingTotal" value="${categoryBudgetTotal - categorySpentTotal}"/>
        <c:set var="categoryUsageRate" value="${categoryBudgetTotal > 0 ? (categorySpentTotal * 100 / categoryBudgetTotal) : 0}"/>

        <section id="overall-budget" class="mt-4 space-y-3">
            <div class="flex items-center justify-between">
                <h2 class="text-sm font-semibold">전체 예산</h2>
                <c:choose>
                    <c:when test="${not empty overallBudget}">
                        <a href="${ctx}/budget/budgets/${overallBudget.budgetId}/edit" class="text-xs text-slate-500">수정</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${ctx}/budget/budgets/new?month=${selectedMonth}" class="text-xs text-slate-500">설정</a>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:choose>
                <c:when test="${not empty overallBudget}">
                    <article class="pv-card p-4">
                        <a href="${ctx}/budget/budgets/${overallBudget.budgetId}" class="block">
                            <div class="flex items-start justify-between">
                                <div>
                                    <div class="text-xs text-slate-500">${overallBudget.targetMonth}</div>
                                    <div class="mt-1 text-base font-semibold">전체 예산 진행률 ${overallBudget.usageRate}%</div>
                                </div>
                                <span class="rounded-full px-2 py-1 text-xs ${overallBudget.usageRate >= 100 ? 'bg-red-100 text-red-700' : 'bg-slate-100 text-slate-700'}">
                                    ${overallBudget.usageRate >= 100 ? '초과' : '진행중'}
                                </span>
                            </div>

                            <div class="mt-3 grid grid-cols-3 gap-2 text-sm">
                                <div class="rounded-xl border border-slate-200 p-3">
                                    <div class="text-xs text-slate-500">목표</div>
                                    <div class="mt-1 font-semibold"><fmt:formatNumber value="${overallBudget.amountLimit}" pattern="#,##0"/></div>
                                </div>
                                <div class="rounded-xl border border-slate-200 p-3">
                                    <div class="text-xs text-slate-500">소진</div>
                                    <div class="mt-1 font-semibold"><fmt:formatNumber value="${overallBudget.spentAmount}" pattern="#,##0"/></div>
                                </div>
                                <div class="rounded-xl border border-slate-200 p-3">
                                    <div class="text-xs text-slate-500">잔여</div>
                                    <div class="mt-1 font-semibold ${overallBudget.remainingAmount < 0 ? 'text-red-600' : ''}">
                                        <fmt:formatNumber value="${overallBudget.remainingAmount}" pattern="#,##0"/>
                                    </div>
                                </div>
                            </div>

                            <div class="mt-3 h-2 overflow-hidden rounded-full bg-slate-200">
                                <div class="h-full ${overallBudget.usageRate >= 100 ? 'bg-red-500' : 'bg-slate-900'}" style="width: ${overallBudget.usageRate > 100 ? 100 : overallBudget.usageRate}%"></div>
                            </div>
                        </a>
                    </article>
                </c:when>
                <c:otherwise>
                    <article class="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-4">
                        <div class="text-sm font-semibold">이번 달 전체 예산이 없습니다.</div>
                        <p class="mt-1 text-xs text-slate-500">전체 예산 없이 카테고리별 예산만 운영할 수도 있습니다.</p>
                        <a href="${ctx}/budget/budgets/new?month=${selectedMonth}" class="mt-3 inline-flex rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm">전체 예산 만들기</a>
                    </article>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="category-budget" class="mt-6 space-y-3">
            <div class="flex items-center justify-between">
                <h2 class="text-sm font-semibold">카테고리별 예산</h2>
                <span class="rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-600">${categoryBudgetCount}건</span>
            </div>

            <c:if test="${categoryBudgetCount > 0}">
                <article class="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm">
                    <div class="text-xs text-slate-500">카테고리별 예산 합계</div>
                    <div class="mt-1 font-semibold"><fmt:formatNumber value="${categoryBudgetTotal}" pattern="#,##0"/></div>
                    <div class="mt-2 grid grid-cols-3 gap-2 text-xs text-slate-600">
                        <div>소진 <fmt:formatNumber value="${categorySpentTotal}" pattern="#,##0"/></div>
                        <div>잔여 <fmt:formatNumber value="${categoryRemainingTotal}" pattern="#,##0"/></div>
                        <div>평균 소진율 <fmt:formatNumber value="${categoryUsageRate}" pattern="0.00"/>%</div>
                    </div>
                </article>
            </c:if>

            <c:forEach var="item" items="${budgets}">
                <c:if test="${not empty item.categoryId}">
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
                </c:if>
            </c:forEach>

            <c:if test="${categoryBudgetCount == 0}">
                <section class="pv-card p-4 text-sm text-slate-500">선택한 월에 조회된 카테고리별 예산이 없습니다.</section>
            </c:if>
        </section>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
