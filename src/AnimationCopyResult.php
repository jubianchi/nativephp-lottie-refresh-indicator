<?php

namespace Jubianchi\NativePHP\LottieRefreshIndicator;

final readonly class AnimationCopyResult
{
    /**
     * @param  list<string>  $copied  the asset paths copied, such as `animations/splash.lottie`
     * @param  list<string>  $warnings
     */
    public function __construct(
        public array $copied,
        public array $warnings,
    ) {}
}
