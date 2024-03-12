import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	server: {
	fs: {
		allow: ['..']
	}
},
	plugins: [sveltekit()],
	define: {
		'process.env.NODE_ENV': '"production"',
	}
});
