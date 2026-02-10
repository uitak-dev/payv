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
            <form method="get" action="${ctx}/budget/budgets" class="flex items-center gap-2">
                <input type="month" name="month" value="${selectedMonth}" class="rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                <button type="submit" class="rounded-xl border border-slate-200 px-3 py-2 text-sm">조회</button>
            </form>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
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

        <section class="pv-card p-4">
            <div class="text-sm font-semibold">예산 추가</div>
            <form method="post" action="${ctx}/budget/budgets" class="mt-3 grid gap-3" data-ajax="true">
                <label>
                    <div class="text-xs text-slate-500">대상 월 (필수)</div>
                    <input name="month" type="month" required class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                </label>

                <label>
                    <div class="text-xs text-slate-500">목표 금액 (필수)</div>
                    <input name="amountLimit" type="number" min="1" inputmode="numeric" required class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                </label>

                <label>
                    <div class="text-xs text-slate-500">카테고리 (선택, 미선택 시 전체 예산)</div>
                    <select name="categoryId" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                        <option value="">전체 예산</option>
                        <c:forEach var="root" items="${categories}">
                            <option value="${root.categoryId}">${root.name}</option>
                        </c:forEach>
                    </select>
                </label>

                <label>
                    <div class="text-xs text-slate-500">메모 (선택)</div>
                    <textarea name="memo" rows="2" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="메모를 입력하세요"></textarea>
                </label>

                <button type="submit" class="rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white">저장</button>
            </form>
        </section>

        <section class="mt-4 space-y-3">
            <c:forEach var="item" items="${budgets}">
                <article class="pv-card p-4">
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

                    <form method="post"
                          action="${ctx}/budget/budgets/${item.budgetId}"
                          class="mt-4 grid gap-2"
                          data-ajax="true"
                          data-method="PUT"
                          data-json="true">
                        <label>
                            <div class="text-xs text-slate-500">대상 월</div>
                            <input name="month" type="month" required value="${item.targetMonth}" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                        </label>
                        <label>
                            <div class="text-xs text-slate-500">목표 금액</div>
                            <input name="amountLimit" type="number" min="1" inputmode="numeric" required value="${item.amountLimit}" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                        </label>
                        <label>
                            <div class="text-xs text-slate-500">카테고리 (선택)</div>
                            <select name="categoryId" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                                <option value="">전체 예산</option>
                                <c:forEach var="root" items="${categories}">
                                    <option value="${root.categoryId}" ${root.categoryId == item.categoryId ? 'selected' : ''}>${root.name}</option>
                                </c:forEach>
                            </select>
                        </label>
                        <label>
                            <div class="text-xs text-slate-500">메모 (선택)</div>
                            <textarea name="memo" rows="2" class="mt-1 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">${item.memo}</textarea>
                        </label>
                        <button type="submit" class="rounded-xl border border-slate-200 px-3 py-2 text-sm">수정</button>
                    </form>

                    <form method="post"
                          action="${ctx}/budget/budgets/${item.budgetId}"
                          class="mt-2"
                          data-ajax="true"
                          data-method="DELETE">
                        <button type="submit" class="w-full rounded-xl border border-red-200 px-3 py-2 text-sm text-red-700">해제</button>
                    </form>
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
