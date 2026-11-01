<?php

namespace Jubianchi\NativePHP\LottieRefreshIndicator\Commands;

use Jubianchi\NativePHP\LottieRefreshIndicator\AnimationCopier;
use Native\Mobile\Plugins\Commands\NativePluginHookCommand;

class CopyAnimationsCommand extends NativePluginHookCommand
{
    protected $signature = 'nativephp:lottie-refresh-indicator:copy-assets';

    protected $description = 'Copy the app\'s Lottie animations into the native project';

    public function handle(): int
    {
        $configured = (string) config('lottie-refresh-indicator.path');
        $source = str_starts_with($configured, '/') ? $configured : base_path($configured);

        if (! is_dir($source)) {
            $this->info("No animations directory at {$configured}: no animation to copy.");

            return self::SUCCESS;
        }

        $result = AnimationCopier::copy($source, $this->buildPath(), $this->platform());

        foreach ($result->copied as $asset) {
            $this->info("{$this->platform()}: {$asset}");
        }
        foreach ($result->warnings as $warning) {
            $this->warn($warning);
        }

        return self::SUCCESS;
    }
}
