function [z, z_true, time_elps,MAX,count,euc_dist] = laplace(true_loc, state_no, eps, DeltaX, T)

tic;
count=0;
euc_dist=0;
%check if true_data is in the possible set 'DeltaX'
possible_set=sum(DeltaX,1);
if(possible_set(state_no)==0)% true_data is not included;
    minDist=100000000000;
    for tmp=1:size(DeltaX,1)
        if(norm(T*true_loc'-T*(DeltaX(tmp,:))')<minDist)
           z_true=T*DeltaX(tmp,:)';
        end
    end
else% true_data is included;
    z_true=T*true_loc';
end


%%%compute drift ratio  before disturb
possible_set=sum(DeltaX,1);
count=0;
%drift_ratio=0;
%size_deltax=size(DeltaX,1);
if(possible_set(state_no)==0)% true_data is not included;
   count+=1;
   %drift_ratio=count/size_deltax;
end


[MAX]=getSensitivity(T,DeltaX);
[tempx,tempy]=IMnoise(eps,MAX,T,DeltaX,true_loc,z_true);
if(MAX==0)  %only 1 state;
   z=z_true;
   time_elps=toc;
   return;
end
temp=[tempx;tempy];
z=z_true+temp;
%display(z_true);
%display(z);
time_elps=toc;

%%%compute dist
dist=zeros(2,1);
dist=(z-(T*true_loc')).^2
euc_dist=sqrt(sum(dist(:)));


end
%over-------------------------------------------


%function [MAX] = getSensitivity(T,DeltaX,z_true) % this is first used

   %MAX=-inf;
  % for tmp=1:size(DeltaX,1)
        %%norm_l1=norm((T*true_loc'-T*(DeltaX(tmp,:))'),1);
        %norm_l1=norm((z_true-T*(DeltaX(tmp,:))'),1);
        %if(norm_l1>MAX)
          % MAX=norm_l1;
        %end
   %end
%end
function [MAX] = getSensitivity(T,DeltaX)

   MAX=-inf;
   Delta=T*DeltaX';
   for i=1:size(Delta,2)
       for j=1:size(Delta,2)
         % if(i==j)
             %continue;
           %end
          tmp=norm((Delta(:,i)-Delta(:,j)),1) %change
         if(tmp>MAX)
           MAX=tmp;
         end
     end
   end
end
%
function [tempx,tempy]=IMnoise(eps,MAX,T,DeltaX,true_loc)

[MAX]=getSensitivity(T,DeltaX);
%eps=MAX/eps;

a=rand;
b=rand;
if(a<=0.5)
     tempx=(MAX/eps)*log10(2*a);

else(a>=0.5)
     tempx=-(MAX/eps)*log10(2-2*a);
end
if(b<=0.5)
     tempy=(MAX/eps)*log10(2*b);

else(b>=0.5)
     tempy=-(MAX/eps)*log10(2-2*b);
end
 %hist(temp);
end
%---------------------------------------------------


%function [temp]=IMnoise(pro,k)

  %pro=k/pro

% pro=1
 %a=rand(1,10)
 % a = []

 %result = zeros(size(a))
 %for  i = 1:length(a)
  %if(a(:,i)<=0.5)
     %result(:,i)=pro*log10(2*a(:,i))
  %else if(a(:,i)>=0.5)
     %result(:,i)=-pro*log10(2-2*a(:,i))
  %end
 %end


 %hist(temp)
%hist(result)
 %end