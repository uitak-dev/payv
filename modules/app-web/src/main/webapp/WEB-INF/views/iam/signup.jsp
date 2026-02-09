<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>회원가입</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="login" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/login" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">로그인</a>
            <div class="text-base font-semibold">회원가입</div>
            <span class="w-14"></span>
        </div>
    </header>

    <main class="px-4 pb-8 pt-4">
        <div class="space-y-6 pt-10">
            <div>
                <h2 class="text-xl font-semibold">Payv 회원가입</h2>
                <p class="mt-1 text-sm text-slate-500">계정 정보를 입력해 주세요.</p>
            </div>

            <c:if test="${not empty error}">
                <p class="rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">회원가입에 실패했습니다. 입력값 또는 중복 이메일을 확인해 주세요.</p>
            </c:if>

            <form class="space-y-4" method="post" action="${ctx}/signup" data-ajax="true">
                <label class="block">
                    <div class="text-sm font-medium">이메일</div>
                    <input name="email" type="email" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2" placeholder="example@example.com"/>
                </label>
                <label class="block">
                    <div class="text-sm font-medium">비밀번호</div>
                    <input name="password" type="password" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2" placeholder="8자 이상 권장"/>
                </label>
                <label class="block">
                    <div class="text-sm font-medium">표시 이름 (선택)</div>
                    <input name="displayName" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2" placeholder="예: 홍길동"/>
                </label>
                <button type="submit" class="mt-2 inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white">회원가입</button>
            </form>

            <a href="${ctx}/login" class="inline-flex w-full items-center justify-center rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700">로그인으로 돌아가기</a>
        </div>
    </main>
</div>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>
