<template>
    <div class="nav-pages-btn">
    <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" width="16" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21"></polygon><line x1="9" y1="3" x2="9" y2="18"></line><line x1="15" y1="6" x2="15" y2="21"></line></svg>
    <span>Navigation des pages</span>
    <svg fill="currentColor" height="12" width="12" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><path d="m7.247 4.86-4.796 5.481c-.566.647-.106 1.659.753 1.659h9.592a1 1 0 0 0 .753-1.659l-4.796-5.48a1 1 0 0 0-1.506 0z"/></svg>
</div>
</template>

<script>
    import { ref,computed } from "vue";
</script>


<style>
        .nav-pages-btn {
            position: fixed;
            bottom: 24px;
            right: 24px;
            background-color: var(--eo-secondary-dark);
            color: var(--ofana-dark);
            padding: 12px 20px;
            border-radius: 12px;
            font-weight: 600;
            box-shadow: 0 4px 16px rgba(0,0,0,0.1);
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 0.9rem;
            text-decoration: none;
            cursor: pointer;
            z-index: 1050;
        }
</style>