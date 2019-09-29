import {Injectable} from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import {BehaviorSubject} from "rxjs/BehaviorSubject";

@Injectable({ providedIn: 'root' })
export class SidebarService {

    private avatarSource = new BehaviorSubject<string>('../../content/images/new/default-avatar.png');
    currentAvatar = this.avatarSource.asObservable();

    constructor() {
    }

    changeAvatar(avatar: string) {
        if(avatar == null){
            this.avatarSource.next('../../content/images/new/default-avatar.png');
        } else {
            this.avatarSource.next(avatar);
        }
    }
}
