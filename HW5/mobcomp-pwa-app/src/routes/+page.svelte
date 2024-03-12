<script>
	import AudioRecorder from '../lib/AudioRecorder.svelte';
    import CameraRecorded from '../lib/CameraRecorded.svelte';
    import ChatApp from '../lib/ChatApp.svelte';
    import PhotoRecorder from '../lib/PhotoRecorder.svelte';
    import HomePage from './HomePage.svelte';
  import HelloWorldPage from './HelloWorldPage.svelte';
  import { onMount } from 'svelte';
  let currentPage = 'home';

  // Listen for hash changes
//  <AudioRecorder />
//<CameraRecorded />
//<ChatApp/>
//<PhotoRecorder />

onMount(() => {
    currentPage = window.location.hash === '#hello' ? 'hello' : 'home';

    window.onhashchange = () => {
      currentPage = window.location.hash === '#hello' ? 'hello' : 'home';
    };
  });
  </script>
{#if currentPage === 'home'}
  <HomePage />
{:else if currentPage === 'hello'}
  <HelloWorldPage />
{/if}

<AudioRecorder />
<CameraRecorded />
<PhotoRecorder />
