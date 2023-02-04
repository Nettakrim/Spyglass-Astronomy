package com.nettakrim.spyglass_astronomy;

import net.minecraft.util.math.MathHelper;

public class KeplerLookup {
    public static int eccentricitySize = 31;
    public static int angleSize = 31;

    //should be around 4.096 kb, the ends are always 0 and pi, so this could be reduced slightly
    public static float[][] table = new float[][] {
        new float[] {0.0f, 0.10134169850289655f, 0.2026833970057931f, 0.30402509550868967f, 0.4053667940115862f, 0.5067084925144828f, 0.6080501910173793f, 0.7093918895202759f, 0.8107335880231724f, 0.9120752865260691f, 1.0134169850289656f, 1.1147586835318621f, 1.2161003820347587f, 1.3174420805376552f, 1.4187837790405518f, 1.5201254775434483f, 1.6214671760463448f, 1.7228088745492411f, 1.8241505730521381f, 1.9254922715550344f, 2.026833970057931f, 2.1281756685608273f, 2.2295173670637243f, 2.330859065566621f, 2.4322007640695174f, 2.5335424625724134f, 2.6348841610753104f, 2.736225859578207f, 2.8375675580811035f, 2.938909256584f, 3.0402509550868966f, 3.141592653589793f}, 
        new float[] {0.0f, 0.1079198962375003f, 0.21576388429229887f, 0.323457228076284f, 0.43092750327562823f, 0.5381056708694041f, 0.6449270587479723f, 0.7513322268406262f, 0.857267698797826f, 0.9626865490383282f, 1.067548840373426f, 1.1718219133827592f, 1.2754805339660633f, 1.3785069097938276f, 1.4808905895951487f, 1.5826282613221405f, 1.683723466074745f, 1.7841862459134874f, 1.8840327400627614f, 1.9832847470433521f, 2.0819692647167085f, 2.1801180203594557f, 2.2777670001826946f, 2.3749559857316465f, 2.471728102630877f, 2.568129385323246f, 2.664208359824769f, 2.7600156451013187f, 2.8556035724711952f, 2.951025821444231f, 3.046337069611941f, 3.141592653589793f},   
        new float[] {0.0f, 0.11504905843587834f, 0.22991979488233089f, 0.3444377366281462f, 0.458435929326723f, 0.5717582617319541f, 0.6842623115856163f, 0.795821617576719f, 0.9063273262887563f, 1.015689206531065f, 1.123836062001156f, 1.2307156037434206f, 1.3362938647788607f, 1.440554250445726f, 1.5434963201812284f, 1.645134393166942f, 1.745496057432979f, 1.844620654129323f, 1.9425577907497849f, 2.0393659256256003f, 2.135111052715822f, 2.229865504809036f, 2.323706884121064f, 2.416717122029333f, 2.508981664188247f, 2.6005887732939335f, 2.691628939045701f, 2.782194383116683f, 2.8723786459479324f, 2.962276241700151f, 3.051982367553178f, 3.141592653589793f},
        new float[] {0.0f, 0.12279711031602378f, 0.24527869442687308f, 0.36713833535108026f, 0.4880872249814624f, 0.6078615209713119f, 0.7262281650739955f, 0.8429889399980812f, 0.9579827201162767f, 1.0710860286165362f, 1.182212131705491f, 1.2913089714513395f, 1.398356264289369f, 1.5033620810727721f, 1.60635918569835f, 1.7074013594536912f, 1.8065598804443217f, 1.903920274874812f, 1.9995794103089133f, 2.0936429637049634f, 2.1862232689436425f, 2.277437528943409f, 2.3674063649217203f, 2.456252668367822f, 2.544100718378153f, 2.6310755268978996f, 2.7173023760848745f, 2.8029065146775447f, 2.8880129833322044f, 2.972746541991488f, 3.0572316751871145f, 3.141592653589793f},  
        new float[] {0.0f, 0.1312427708068687f, 0.26198699906504214f, 0.39175284632589763f, 0.5200962048973732f, 0.6466226482323484f, 0.7709973905023652f, 0.8929509130313424f, 1.0122804516900719f, 1.128847947504844f, 1.242575299140427f, 1.3534378247220886f, 1.4614567779412926f, 1.566691621308114f, 1.6692325783490283f, 1.769193815927461f, 1.8667074516627011f, 1.961918465359588f, 2.054980508675974f, 2.146052553614245f, 2.235296290663369f, 2.3228741750877844f, 2.4089480192000514f, 2.493678034895497f, 2.5772222409176324f, 2.659736160968476f, 2.741372750414563f, 2.822282500109323f, 2.9026136753121095f, 2.9825126556550297f, 3.062124348569057f, 3.141592653589793f},       
        new float[] {0.0f, 0.1404777071646996f, 0.2802132952745213f, 0.41850011639983775f, 0.5546983544699309f, 0.6882590388972403f, 0.818738932283205f, 0.9458061132038479f, 1.0692374053636438f, 1.1889096238553596f, 1.3047868804250453f, 1.4169060265042772f, 1.5253618951679904f, 1.6302934946377052f, 1.731871835003307f, 1.8302896862792386f, 1.9257532985606152f, 2.018475947454401f, 2.108673080862038f, 2.1965588120188246f, 2.2823435077229512f, 2.3662322441426875f, 2.448423934652669f, 2.529110968045021f, 2.608479227366998f, 2.6867083877097553f, 2.763972414891211f, 2.8404402062096974f, 2.916276329707187f, 2.9916418302135765f, 3.0666950793950387f, 3.141592653589793f},    
        new float[] {0.0f, 0.1506092042620735f, 0.30015229432791724f, 0.44762715814564413f, 0.5921503261576709f, 0.7329953756270011f, 0.8696121805458867f, 1.0016280584215618f, 1.1288346856907283f, 1.2511658702198174f, 1.3686710896167025f, 1.4814886780527612f, 1.5898212313862174f, 1.6939145971269478f, 1.7940409003227193f, 1.8904854667929274f, 1.983537186086262f, 2.073481731266248f, 2.1605970472111355f, 2.245150577353222f, 2.327397783180971f, 2.4075815991785516f, 2.485932546707095f, 2.5626692989146678f, 2.6379995442053024f, 2.712121039131329f, 2.785222774605964f, 2.8574862040235103f, 2.9290865000257624f, 3.0001938197897737f, 3.0709745680546154f, 3.141592653589793f}, 
        new float[] {0.0f, 0.1617634921835046f, 0.32202953677878765f, 0.4794124235999738f, 0.6327296169375723f, 0.781059222169354f, 0.9237597703477924f, 1.0604575606253828f, 1.1910118995144339f, 1.3154694328536432f, 1.4340167207032828f, 1.5469370307135448f, 1.6545743276949176f, 1.7573052417193316f, 1.8555184731635406f, 1.9496004751652307f, 2.0399260910132617f, 2.1268529120189927f, 2.210718317334688f, 2.2918383773885562f, 2.370508005189248f, 2.4470019083918455f, 2.521576027148419f, 2.594469242156988f, 2.6659052099134057f, 2.7360942339986853f, 2.805235117558206f, 2.8735169671783565f, 2.94112093538014f, 3.008221900329288f, 3.0749900888438755f, 3.141592653589793f},    
        new float[] {0.0f, 0.17408992112584082f, 0.3461069709042661f, 0.514169041494185f, 0.676733013216671f, 0.8326747571325943f, 0.9812988857883957f, 1.1222947253792996f, 1.2556623976104402f, 1.3816306276718198f, 1.5005808681588582f, 1.612984966466076f, 1.7193581664746052f, 1.8202261204916248f, 1.9161032872977575f, 2.0074798942372105f, 2.094814978380013f, 2.178533537232779f, 2.259026329035527f, 2.3366512910754453f, 2.4117358746009803f, 2.4845798358016196f, 2.5554581914934733f, 2.624624163574769f, 2.692312013193728f, 2.7587397156538382f, 2.8241114589765393f, 2.8886199688409366f, 2.952448674534131f, 3.015773737431538f, 3.0787659672952343f, 3.141592653589793f},     
        new float[] {0.0f, 0.18776623366867884f, 0.37268952819772977f, 0.5522477310391362f, 0.7244732657308705f, 0.8880543037313468f, 1.0423112234538343f, 1.1870911813748095f, 1.322630147398026f, 1.4494192473213872f, 1.5680946954013593f, 1.6793567104452845f, 1.7839149185771062f, 1.8824546536838338f, 1.975618278456305f, 2.063996578984412f, 2.148126520606399f, 2.2284927741218934f, 2.305531296991734f, 2.3796338829786134f, 2.4511530217413307f, 2.520406690208447f, 2.587682875409278f, 2.6532437379802136f, 2.717329390864282f, 2.780161305058256f, 2.84194537433617f, 2.902874680584832f, 2.9631320050958485f, 3.022892131600793f, 3.0823239856946993f, 3.141592653589793f},       
        new float[] {0.0f, 0.2030052668991479f, 0.40213279773089194f, 0.5940389336260227f, 0.776273329915733f, 0.9473874045808686f, 1.106832023842957f, 1.2547433291197478f, 1.3917089208899769f, 1.5185689907512683f, 1.6362710432137224f, 1.7457752970816671f, 1.8479994160345319f, 1.9437903731971362f, 2.0339135103347457f, 2.119051700567848f, 2.19980995748732f, 2.2767226123394355f, 2.3502613738509437f, 2.420843341614765f, 2.488838503588719f, 2.554576517100901f, 2.6183527235146156f, 2.68043342656396f, 2.7410605028612642f, 2.8004554282166456f, 2.8588228059638836f, 2.916353479764131f, 2.97322730697843f, 3.0296156616833336f, 3.085683729841491f, 3.141592653589793f},
        new float[] {0.0f, 0.2200635281595988f, 0.4348518415382955f, 0.6399734627784904f, 0.8324574055085479f, 1.0108273967072536f, 1.1748389046293115f, 1.3250875608785266f, 1.4626443885472047f, 1.5887844324119083f, 1.704813458777306f, 1.8119712241485348f, 1.9113858874808813f, 2.004059104282551f, 2.090867853683194f, 2.172574383192264f, 2.2498393235557983f, 2.3232353213461483f, 2.393259886846721f, 2.4603469101978437f, 2.5248767005386994f, 2.587184602615566f, 2.647568333687347f, 2.7062942129329386f, 2.763602455416679f, 2.819711689145211f, 2.874822835431927f, 2.929122474037944f, 2.9827857974474226f, 3.0359792440091065f, 3.088862887769812f, 3.141592653589793f},        
        new float[] {0.0f, 0.2392522380001554f, 0.47133106496343385f, 0.6905206511436988f, 0.8933379843485836f, 1.0784757982056323f, 1.2462415919536798f, 1.3978984654911508f, 1.5351394953442805f, 1.6597502631113465f, 1.7734260541046423f, 1.877690717611903f, 1.973873593691288f, 2.0631158425570937f, 2.1463895188412985f, 2.2245205426617636f, 2.2982112722783037f, 2.3680608525908435f, 2.4345827727598586f, 2.498219662825293f, 2.5593556097261536f, 2.618326351265399f, 2.675427704288276f, 2.7309225477473036f, 2.7850466355899175f, 2.8380134692552845f, 2.8900184194341287f, 2.9412422529878777f, 2.991854193496302f, 3.042014622199277f, 3.091877509360454f, 3.141592653589793f},   
        new float[] {0.0f, 0.260951633886619f, 0.5121348103604499f, 0.74618257516993f, 0.9591982485131622f, 1.1503653440079766f, 1.3208739765761375f, 1.472890934197722f, 1.6088631638390283f, 1.7311422590610044f, 1.8418235085920789f, 1.9427030969445196f, 2.035291139462682f, 2.120846354002743f, 2.200415622595153f, 2.274871160393884f, 2.3449427109352854f, 2.4112443417905807f, 2.474296297768525f, 2.534542652365725f, 2.592365518513635f, 2.6480964980025563f, 2.7020259388723513f, 2.754410462753829f, 2.805479130997873f, 2.85543854197683f, 2.9044770911118576f, 2.9527685776979378f, 3.0004753060434277f, 3.0477508006560234f, 3.0947422344243685f, 3.141592653589793f},
        new float[] {0.0f, 0.285629590351677f, 0.5579179041911198f, 0.8074825033444609f, 1.0302695614192505f, 1.2264431803415674f, 1.3984901183536953f, 1.549726792302583f, 1.683461980622722f, 1.8026392191849383f, 1.9097405349369312f, 2.006806863079818f, 2.0954993920864307f, 2.177167637000038f, 2.252911021164616f, 2.3236302281471373f, 2.390068433531665f, 2.452843832023671f, 2.512475119416371f, 2.5694014384063757f, 2.6239980358890964f, 2.6765886187045638f, 2.7274551719443574f, 2.7768458256818818f, 2.8249812181016756f, 2.8720596982050672f, 2.918261632377299f, 2.9637530200426756f, 3.0086885796193976f, 3.0532144333751816f, 3.0974704958797115f, 3.141592653589793f},      
        new float[] {0.0f, 0.3138659424778849f, 0.6094346472451124f, 0.8749453526076343f, 1.1067045805517426f, 1.306556449222334f, 1.4787657688380058f, 1.6280261000277279f, 1.7585741306087785f, 1.8739350057252169f, 1.9769402317632925f, 2.069834305399363f, 2.154393083502053f, 2.2320274683335612f, 2.30386666461358f, 2.37082258424484f, 2.4336389089696944f, 2.4929283086551024f, 2.5492007271498545f, 2.6028849941621752f, 2.6543454599563154f, 2.7038949103038634f, 2.751804689628496f, 2.7983127180247185f, 2.8436299112376857f, 2.8879453843023506f, 2.931430726160484f, 2.9742435645567586f, 3.016530590928645f, 3.058430178916141f, 3.1000747040400958f, 3.141592653589793f},
        new float[] {0.0f, 0.3463842922376607f, 0.6675435553016786f, 0.9490678757022708f, 1.1885477676925698f, 1.390443078747295f, 1.5613065878954386f, 1.70738267592249f, 1.8338445561036179f, 1.9447498563578955f, 2.0432209308087046f, 2.131654611671391f, 2.2119012965371017f, 2.2854033076746f, 2.3532977305554774f, 2.416491839928549f, 2.4757183542394063f, 2.5315761154901577f, 2.5845602864036463f, 2.635084989736093f, 2.683500465669474f, 2.7301062220517536f, 2.77516123078475f, 2.818891928611134f, 2.861498573375568f, 2.9031603605845984f, 2.944039601211477f, 2.9842851874777465f, 3.0240355200931917f, 3.0634210321841935f, 3.102566417793542f, 3.141592653589793f},
        new float[] {0.0f, 0.38409345684116314f, 0.7332033957738999f, 1.0302769205176712f, 1.2757067238603976f, 1.4777307835088076f, 1.6456634881731138f, 1.7873828068427873f, 1.9089401973197813f, 2.0148403051951496f, 2.1084213790324546f, 2.19217564077605f, 2.2679871280521837f, 2.3373008622067424f, 2.4012417858768544f, 2.4606985727902093f, 2.5163832026977935f, 2.5688738103626925f, 2.6186459096528605f, 2.666095455451626f, 2.7115561083628297f, 2.755312333052041f, 2.7976094677183743f, 2.838661567919423f, 2.8786575992531382f, 2.917766395291153f, 2.9561406867721054f, 2.9939204303161406f, 3.0312356097938724f, 3.068208644270329f, 3.104956508635543f, 3.141592653589793f},   
        new float[] {0.0f, 0.4281409238246609f, 0.8074537005044786f, 1.118875919308697f, 1.367929533167143f, 1.5679468207018914f, 1.731354556395111f, 1.86762568687712f, 1.9835642626635004f, 2.0840073257217275f, 2.17242434166593f, 2.2513446671395743f, 2.322646885537539f, 2.3877526248580563f, 2.4477572148575857f, 2.503518950789714f, 2.55572107024273f, 2.604915519959758f, 2.651554387368255f, 2.6960128439614763f, 2.7386061563628434f, 2.7796024893649816f, 2.8192326825352247f, 2.8576978227049747f, 2.8951751934104957f, 2.9318230181356006f, 2.9677843010163243f, 3.0031899897940355f, 3.038161630375902f, 3.0728136432291353f, 3.1072553242538374f, 3.141592653589793f},
        new float[] {0.0f, 0.47998019125276215f, 0.8913703166284481f, 1.2149832717504276f, 1.4647945648008633f, 1.6605398313225148f, 1.817892022734732f, 1.9477439946243464f, 2.057468772827104f, 2.152102646865919f, 2.235158944936125f, 2.309148526055192f, 2.3759092198323177f, 2.436816709967874f, 2.4929221521598643f, 2.545043953268256f, 2.5938303325615446f, 2.6398028715254864f, 2.6833874350565283f, 2.7249365358944306f, 2.764745792491672f, 2.8030662391742402f, 2.8401136778778984f, 2.876075890049107f, 2.911118281827773f, 2.9453883704556847f, 2.979019407097114f, 3.012133353318861f, 3.0448433740766294f, 3.0772559718645467f, 3.10947285989295f, 3.141592653589793f},
        new float[] {0.0f, 0.5414516199904407f, 0.9859849020965378f, 1.3184715338149475f, 1.5657191661882468f, 1.7549132938019345f, 1.9048120587452924f, 2.0274232458284986f, 2.130465078276799f, 2.2190335537193295f, 2.2966022885937836f, 2.3656136932257805f, 2.4278346445402064f, 2.4845763370830523f, 2.536834183649409f, 2.5853793848903437f, 2.6308204623904303f, 2.673645619478414f, 2.714252555134416f, 2.7529698761860506f, 2.790072764604666f, 2.825794641507771f, 2.8603359936832136f, 2.8938711584155863f, 2.926553619895767f, 2.9585202087253117f, 2.9898944863892014f, 3.0207895212458142f, 3.051310210274812f, 3.0815552642583204f, 3.1116189485088195f, 3.141592653589793f},    
        new float[] {0.0f, 0.6148700131021028f, 1.0921593110206702f, 1.4289223091358987f, 1.6699917625634912f, 1.8504682191726225f, 1.9917050683871744f, 2.1064191374131043f, 2.20243260987124f, 2.284766849500074f, 2.3567810646036356f, 2.4208069491140676f, 2.478515971691434f, 2.5311403816579316f, 2.5796111467247576f, 2.6246469507123384f, 2.66681335056332f, 2.706563160115858f, 2.7442646854797514f, 2.780221897383617f, 2.814689129158858f, 2.847881981748595f, 2.879985552839117f, 2.911160747940924f, 2.941549197606667f, 2.971277150107419f, 3.0004586044879917f, 3.029197877515906f, 3.0575917486370248f, 3.085731292624944f, 3.1137034856066164f, 3.141592653589793f},
        new float[] {0.0f, 0.7030970096494752f, 1.2104160222739626f, 1.545615967044839f, 1.77682814188078f, 1.9466514531341486f, 2.078244735138111f, 2.1845729740708473f, 2.273326758987743f, 2.349333049596676f, 2.4157741681200493f, 2.4748374606953156f, 2.528080349347299f, 2.5766455599054146f, 2.6213935082401094f, 2.662986804917094f, 2.701945974936865f, 2.7386872616095674f, 2.7735489323928335f, 2.806810002656512f, 2.838703838012481f, 2.8694282227665058f, 2.8991529435394066f, 2.9280255965661315f, 2.9561761069524364f, 2.9837203028624377f, 3.010762790027766f, 3.037399305438498f, 3.0637186831693812f, 3.0898045333745383f, 3.115736713266103f, 3.141592653589793f},
        new float[] {0.0f, 0.8095453383344077f, 1.3407486391140275f, 1.6675743852145413f, 1.8854483310503554f, 2.0430059928847473f, 2.164215434152072f, 2.261826408610186f, 2.3431875430383124f, 2.4128323931463567f, 2.4737176796829754f, 2.5278614520005194f, 2.5766939013908905f, 2.6212611155749057f, 2.662349086345378f, 2.7005622604147437f, 2.7363750404596408f, 2.7701665785908753f, 2.802244909633666f, 2.8328640864502685f, 2.862236604977194f, 2.890542588061474f, 2.917936695237259f, 2.9445534096686203f, 2.9705111499649126f, 2.9959155207157315f, 3.020861925929797f, 3.0454377085611086f, 3.0697239372880363f, 3.0937969325364056f, 3.1177296034544217f, 3.141592653589793f},    
        new float[] {0.0f, 0.938009035547758f, 1.4824709704875556f, 1.7936667955595755f, 1.9951675979790844f, 2.139221582967059f, 2.2495397334754816f, 2.338238241349467f, 2.412151786615123f, 2.475445103135045f, 2.530814332018584f, 2.580091309816983f, 2.624570604179543f, 2.6651974755452654f, 2.7026814467186053f, 2.7375678748379277f, 2.770284705393484f, 2.801173975461835f, 2.8305136232738253f, 2.8585329525061374f, 2.8854238348309798f, 2.91134898470564f, 2.9364481825498356f, 2.960843035189331f, 2.9846406778318917f, 3.0079367006555904f, 3.030817502042853f, 3.0533622154236078f, 3.075644318796258f, 3.097733009704157f, 3.1196944101779533f, 3.141592653589793f},
        new float[] {0.0f, 1.0921532412707822f, 1.6341947961273235f, 1.9227759904173283f, 2.1054962298780566f, 2.2351878950601525f, 2.334310896606385f, 2.414008365107579f, 2.4804734292740602f, 2.5374499697709654f, 2.5873510784459635f, 2.6318123714567827f, 2.6719883453642357f, 2.7087215665265503f, 2.742644446036487f, 2.7742431895496593f, 2.803899490400904f, 2.831918586990155f, 2.858548671451337f, 2.883994645467318f, 2.9084280832939426f, 2.9319945909712484f, 2.954819341715229f, 2.9770113113023364f, 2.9986665728379114f, 3.0198709024728356f, 3.040701875572269f, 3.061230583901957f, 3.0815230707347325f, 3.1016415574306015f, 3.1216455188232035f, 3.141592653589793f},      
        new float[] {0.0f, 1.2745135001094623f, 1.7940278437692616f, 2.0540132547396737f, 2.216250229385159f, 2.331059816571087f, 2.4188407611837475f, 2.489518386486078f, 2.548560470635788f, 2.599258889013324f, 2.643731674269027f, 2.68341370660971f, 2.71931796307652f, 2.752184137236782f, 2.7825678561054086f, 2.810896675735453f, 2.8375065692417096f, 2.862666478417822f, 2.886595299861559f, 2.9094739279025865f, 2.9314539811150575f, 2.9526642517925032f, 2.97321556009903f, 2.993204470705425f, 3.012716186060395f, 3.03182683625024f, 3.050605322449377f, 3.0691148282085763f, 3.087414083413188f, 3.105558445337061f, 3.1236008470383894f, 3.141592653589793f},
        new float[] {0.0f, 1.4851487885012211f, 1.9600397266258365f, 2.1869789962069373f, 2.327692871957737f, 2.427358392692135f, 2.5037448399348374f, 2.5654085300198f, 2.617046227565671f, 2.661483436656008f, 2.700539008849927f, 2.735446465660201f, 2.767077799276409f, 2.7960706748171504f, 2.822904783929871f, 2.8479497858281233f, 2.8714965680133555f, 2.8937782984659868f, 2.914985004474854f, 2.9352739191729285f, 2.9547769865198505f, 2.9736064137401357f, 2.9918588546634712f, 3.0096186160545564f, 3.026960156185484f, 3.0439500643172224f, 3.060648655870207f, 3.0771112814427486f, 3.0933894226264464f, 3.109531630068364f, 3.1255843470636946f, 3.141592653589793f},
        new float[] {0.0f, 1.7208182408265749f, 2.130996589704536f, 2.3221126901309885f, 2.4407680273987395f, 2.5251644548460432f, 2.590115816069801f, 2.642736988822784f, 2.686936944703121f, 2.725072037005476f, 2.758662715665813f, 2.788742617203826f, 2.8160441326158607f, 2.8411040168379924f, 2.864326880603929f, 2.8860251224570797f, 2.906445011076774f, 2.9257842793862188f, 2.944204328338782f, 2.9618389014084143f, 2.9788003861738463f, 2.9951844831647754f, 3.0110737283644053f, 3.0265401966532646f, 3.0416476112288997f, 3.0564530168787156f, 3.071008130027627f, 3.08536044789869f, 3.0995541780510734f, 3.113631034917989f, 3.127630939779603f, 3.141592653589793f},
        new float[] {0.0f, 1.9762730850041614f, 2.307454366866342f, 2.4613185707872764f, 2.557602512395018f, 2.626562990727848f, 2.6799274466846814f, 2.723350725213211f, 2.759953853089977f, 2.7916263798752463f, 2.819592345486463f, 2.844686808425105f, 2.8675033162933974f, 2.8884780953117546f, 2.907940813214219f, 2.926146592862502f, 2.943296975502741f, 2.9595540949579497f, 2.975050530697657f, 2.9898963256534645f, 3.0041840939920883f, 3.01799281221702f, 3.0313906842572176f, 3.044437343875629f, 3.0571855757657067f, 3.069682682783547f, 3.0819715906140805f, 3.0940917565426633f, 3.1060799320120163f, 3.1179708168252005f, 3.1297976346230394f, 3.141592653589793f},
        new float[] {0.0f, 2.249225880693658f, 2.4939268243007384f, 2.6095795788440994f, 2.682903499006657f, 2.735895158146652f, 2.777171444252641f, 2.8109256331602577f, 2.8394895386664123f, 2.864283673373987f, 2.886233022530646f, 2.905971414786064f, 2.9239510845545373f, 2.940505507976961f, 2.9558874442723364f, 2.970293013078512f, 2.983877511811677f, 2.996766141677754f, 3.0090614835269154f, 3.0208488354161576f, 3.032200106122153f, 3.043176710979595f, 3.053831764617724f, 3.0642117695941566f, 3.0743579382662287f, 3.084307244595789f, 3.0940932752839303f, 3.10374693100224f, 3.113297015613364f, 3.122770742306076f, 3.132194179316249f, 3.141592653589793f},
        new float[] {0.0f, 2.553417992612795f, 2.7066205553064373f, 2.781387188681549f, 2.829590351160528f, 2.8647951181563363f, 2.8924175899470326f, 2.9151284170732508f, 2.9344273480466394f, 2.9512350341167863f, 2.9661546890333277f, 2.979601779284236f, 2.991874028922002f, 3.003191801399332f, 3.0137226685901584f, 3.0235970283955744f, 3.032918403620075f, 3.0417704494520974f, 3.0502218524085585f, 3.0583298376798442f, 3.066142734011482f, 3.0737018857730702f, 3.0810431038883914f, 3.088197785441469f, 3.0951937917621777f, 3.1020561483581646f, 3.1088076122646364f, 3.1154691402156462f, 3.1220602826170896f, 3.1285995224246697f, 3.1351045739258963f, 3.141592653589793f}
    };

    public static float getAt(float e, float M) {
        if (M > 1) {
            return MathHelper.TAU-getRaw(e*eccentricitySize, (2-M)*angleSize);
        } else {
            return getRaw(e*eccentricitySize, M*angleSize);
        }
    }

    private static float getRaw(float e, float M) {
        int upperE = MathHelper.ceil(e);
        int lowerE = MathHelper.floor(e);
        float modE = e%1;

        int upperM = MathHelper.ceil(M);
        int lowerM = MathHelper.floor(M);
        float modM = M%1;


        float averageE = (
            ((table[lowerE][lowerM]*(1-modM)) + (table[lowerE][upperM]*modM))*(1-modE)
        ) + (
            ((table[upperE][lowerM]*(1-modM)) + (table[upperE][upperM]*modM))*modE
        );

        return averageE;
    }
}