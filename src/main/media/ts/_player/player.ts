import { ApiService } from "../_service/api.service";
import { Sources } from "../_datamodel/sources";
import { ShareOptions, ShareType, DefaultShareTranslation, ShareTranslation } from "../_datamodel/share";

const PREFIX_OBJECT = "receive";
const PREFIX_SLOT = "rc";

export class Player {

  private api: ApiService;

  private options: { [key: string]: string };

  private playerOptions = {
    playbackRates: [0.75, 1, 1.25, 1.5],
    controlBar: {
      subsCapsButton: false
    }
  };

  private videojs = (<any>window).videojs;

  player: any;

  baseURL: string;

  playerId: string;

  branding: boolean;


  constructor(baseURL: string, playerId: string = "dbt-player", branding: boolean = false, options: { [key: string]: string } = {}) {
    this.baseURL = baseURL && baseURL + (baseURL.lastIndexOf("/") === baseURL.length - 1 ? "" : "/");
    this.playerId = playerId;
    this.branding = branding;
    this.options = options;

    this.api = new ApiService(this.baseURL);

    this.init();
  }

  private init() {
    if (!this.videojs) {
      throw new Error("Couldn't find videojs.");
    }

    const id = "#" + this.playerId;
    if (!this.videojs.getPlayer(id)) {
      this.player = this.videojs(id, this.playerOptions, () => {
        this.initBrand();
        this.initSharePlugin();
        this.initPosterPreview();
        this.updateSources();
      });
    } else {
      this.player = this.videojs(id, null, () => {
        this.initBrand();
        this.initSharePlugin();
        this.initPosterPreview();
        this.updateSources();
      });
    }
  }

  private initBrand() {
    if (this.branding === true) {
      const elmPlayer = this.player.el();

      if (elmPlayer) {
        if (!elmPlayer.querySelector(".brand")) {
          const brand = document.createElement("a");
          brand.setAttribute("class", "brand");

          brand.addEventListener("click", (e: Event) => {
            e.stopPropagation();
          });

          elmPlayer.appendChild(brand);

          this.player.on("play", () => {
            brand.classList.toggle("hide");
          });
          this.player.on("pause", () => {
            brand.classList.toggle("hide");
          });
        }
      }
    }
  };

  private initPosterPreview() {
    let intPoster: number;
    const cmpPoster = this.getComponent("PosterImage");
    const elmPoster = cmpPoster.el();

    if (elmPoster) {
      elmPoster.addEventListener("mouseover", () => {
        intPoster = window.setInterval(function () {
          const posters = elmPoster.children.length;
          const i = parseInt(elmPoster.getAttribute("data-thumb-index"), 10) || 0;
          const prevI = i === 0 ? posters - 1 : i - 1;

          (elmPoster.style.backgroundImage) && (elmPoster.style.backgroundImage = "");
          elmPoster.children[prevI].style.display = "none";
          elmPoster.children[i].style.display = "inline-block";

          elmPoster.setAttribute("data-thumb-index", i < posters - 1 ? i + 1 : 0);
        }, 1000);
      });
      elmPoster.addEventListener("mouseout", () => {
        if (intPoster) {
          window.clearInterval(intPoster);
        }
      });
    }
  }

  private initSharePlugin() {
    if (this.player.share) {
      const lang = this.getCurrentLang();
      const trans: ShareTranslation = DefaultShareTranslation[lang];

      this.videojs.addLanguage(lang, {
        "Share": trans.title,
        "Direct Link": trans.directLink,
        "Embed Code": trans.embedCode,
        "Copy": trans.copy,
        "Copied": trans.copied
      });
    }
  }

  private preloadImage(url: string, anImageLoadedCallback: () => void): HTMLImageElement {
    const img = new Image();

    img.onload = anImageLoadedCallback;
    img.src = url;

    return img;
  }

  private preloadImages(urls: Array<string>, allImagesLoadedCallback: (images: Array<HTMLImageElement>) => void) {
    const images: Array<HTMLImageElement> = [];
    let loadedCounter = 0;
    const toBeLoadedNumber = urls.length;

    urls.forEach((url) => {
      images.push(this.preloadImage(url, () => {
        loadedCounter++;
        if (loadedCounter === toBeLoadedNumber) {
          allImagesLoadedCallback(images);
        }
      }));
    });
  }

  private updateBrand() {
    const elmPlayer = this.player.el();

    if (elmPlayer) {
      let href = this.baseURL;

      if (this.options) {
        if (this.options.objId) {
          href += ["receive", this.options.objId].join("/");
        } else if (this.options.slotId) {
          href += ["rc", this.options.slotId].join("/");
        }
      }

      const brand = elmPlayer.querySelector(".brand");
      if (brand) {
        brand.setAttribute("href", href);
      }
    }
  }

  private updatePosterPreview(thumbs: Array<string>) {
    const cmpPoster = this.getComponent("PosterImage");
    const elmPoster = cmpPoster.el();

    if (elmPoster) {
      this.preloadImages(thumbs, (images) => {
        const thumbImg: Array<HTMLImageElement> = images;

        let child = elmPoster.lastElementChild;
        while (child) {
          elmPoster.removeChild(child);
          child = elmPoster.lastElementChild;
        }

        thumbImg.forEach((img) => {
          img.style.display = "none";
          elmPoster.appendChild(img);
        });
      });
    }
  }

  private updateSharePlugin() {
    if (this.player.share) {
      const directUrl = this.buildShare(ShareType.DIRECT);
      const embedCode = this.buildShare();

      var shareOptions = {
        socials: [],
        url: directUrl,
        embedCode: embedCode && embedCode.replace(/"/g, "'")
      }

      this.player.share(shareOptions);
    }
  }

  private updateSources() {
    this.updateBrand();
    this.updateSharePlugin();

    if (this.player && this.player.src && this.player.poster) {
      this.api.getSources(this.options.id, (data: Sources) => {
        const sources = data;
        this.player.src(sources.source);

        this.api.getThumbs(this.options.id, (data: Array<string>) => {
          const thumbs = data;

          if (thumbs && thumbs.length > 0) {
            this.player.poster(thumbs[0]);
            this.updatePosterPreview(thumbs);
          }
        });
      });
    }
  }

  private getComponent(name: string): any | null {
    const childs = this.player.children();
    for (let i = 0; i < childs.length; i++) {
      const child = childs[i];
      if (child && child.name_ && child.name_ === name) {
        return child;
      }
    }
    return null;
  }

  private getCurrentLang() {
    const siteLang = document.querySelector("html[lang]");
    return siteLang && siteLang.getAttribute("lang") || window.navigator.language.slice(0, 2) || "de";
  }

  public changeOptions(options: { [key: string]: string } = {}) {
    this.options = options;

    if (!this.options.objId && !this.options.slotId) {
      const href = location.href.lastIndexOf("?") !== -1 && location.href.slice(0, location.href.lastIndexOf("?")) || location.href;

      this.options.objId = href.indexOf(PREFIX_OBJECT) !== -1 && href.slice(href.indexOf(PREFIX_OBJECT) + PREFIX_OBJECT.length + 1);
      this.options.slotId = href.indexOf(PREFIX_SLOT) !== -1 && href.slice(href.indexOf(PREFIX_SLOT) + PREFIX_SLOT.length + 1);
    }

    this.updateSources();
  }

  private clipboardEvent(event: Event) {
    const elm: HTMLElement = <HTMLElement>event.target || <HTMLElement>event.currentTarget;
    const target = elm.getAttribute("data-clipboard-target");
    const targetElm: HTMLInputElement = target && document.querySelector(target);

    if (targetElm) {
      if (!window.navigator.clipboard) {
        try {
          targetElm.focus();
          targetElm.select();
          if (!document.execCommand("copy")) {
            event.stopPropagation();
          }
        } catch (err) {
          console.error("Oops, unable to copy", err);
        }
        return;
      }
      window.navigator.clipboard.writeText(targetElm.value).then(function () {
      }, function (err) {
        console.error("Oops, unable to copy", err);
      });
    }

  }

  public registerClipboardCopy(selector: string = "*[data-clipboard-target]") {
    const elms = document.querySelectorAll(selector);
    for (let i = 0; i < elms.length; i++) {
      elms[i].addEventListener("click", this.clipboardEvent);
    }
  }

  public buildShare(type: ShareType = ShareType.EMBED_CODE, options: ShareOptions = {}): string | null {
    if (this.options && this.options.id) {
      let directUrl = this.baseURL;
      let embedUrl = `${this.baseURL}rsc/media/embed/${this.options.id}`;

      if (this.options.objId) {
        embedUrl += `?objId=${this.options.objId}`;
        directUrl += ["receive", this.options.objId].join("/");
      } else if (this.options.slotId) {
        embedUrl += `?objId=${this.options.slotId}`;
        directUrl += ["rc", this.options.slotId].join("/");
      }

      const htmlTemplate = `<iframe width="${(options.width || 560)}" height="${(options.height || 315)}" frameborder="0" allowfullscreen="true" src="${embedUrl}"></iframe>`;

      if (type === ShareType.EMBED_URL) {
        return embedUrl;
      } if (type === ShareType.DIRECT) {
        return directUrl;
      }

      return htmlTemplate;
    }

    return null;
  }

  public buildShareModal(parentSel: string = "body") {
    const trans: ShareTranslation = DefaultShareTranslation[this.getCurrentLang()];

    if (this.options && this.options.id) {
      const id = `#share-${this.playerId}`;
      const htmlTemplate = `
      <div class="modal fade" id="share-${this.playerId}" data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="share-bd-${this.playerId}"
        aria-hidden="true"
      >
        <div class="modal-dialog modal-dialog-centered" role="document">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="share-bd-${this.playerId}">${trans.title}</h5>
              <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
              </button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label for="share-code-${this.playerId}">${trans.embedCode}</label>
                <textarea class="form-control" id="share-code-${this.playerId}" rows="5" readonly="true">${this.buildShare()}</textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button id="share-code-btn-${this.playerId}" type="button" class="btn btn-primary" data-dismiss="modal">${trans.copy}</button>
            </div>
          </div>
        </div>
      </div>
      `;

      const parent: HTMLElement = document.querySelector(parentSel);
      if (parent) {
        const modal = document.querySelector(id);
        if (!modal) {
          parent.insertAdjacentHTML("beforeend", htmlTemplate);
          this.registerClipboardCopy(`#share-code-btn-${this.playerId}`);
        } else {
          var input: HTMLInputElement = document.querySelector(`share-code-${this.playerId}`);
          input.value = this.buildShare();
        }
        return id;
      }
    }

    return null;
  }


}