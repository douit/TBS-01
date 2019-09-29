import {Injectable} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";

@Injectable({ providedIn: 'root' })
export class LocalizationResolverService{

    private LANGUAGE_KEY:string = 'lang';
    private currentLang;

    constructor(private translate:TranslateService){
        this.translate.use('en');
    }


    translator():TranslateService{
        return this.translate;
    }

    /**
     *
     */
    init():void{
        this.currentLang = this.getCurrentLanguage();
        if(!this.currentLang){
            this.currentLang = 'en';
        }
        this.translator().use(this.currentLang);
    }

    /**
     *
     * @param {string} language
     */
    changeLanguage(language:string):void{
        this.storeLanguage(language);
        window.location.reload();
    }


    storeLanguage(language:string):void{
        localStorage.setItem(this.LANGUAGE_KEY,language);
    }

    getCurrentLanguage():string{
        let currentLanguage  = localStorage.getItem(this.LANGUAGE_KEY);
        if(!currentLanguage){
            this.storeLanguage('en');
            currentLanguage = 'en';
        }
        return currentLanguage;
    }
}

